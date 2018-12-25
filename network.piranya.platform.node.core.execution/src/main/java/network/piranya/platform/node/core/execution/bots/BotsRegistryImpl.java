package network.piranya.platform.node.core.execution.bots;

import static network.piranya.platform.node.utilities.CollectionUtils.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import network.piranya.platform.api.exceptions.BotNotFoundException;
import network.piranya.platform.api.exceptions.InvalidParameterException;
import network.piranya.platform.api.exceptions.PiranyaException;
import network.piranya.platform.api.extension_models.Data;
import network.piranya.platform.api.extension_models.ExtensionException;
import network.piranya.platform.api.extension_models.Input;
import network.piranya.platform.api.extension_models.InputMetadata;
import network.piranya.platform.api.extension_models.ManagerialExtensionContext;
import network.piranya.platform.api.extension_models.Parameters;
import network.piranya.platform.api.extension_models.SearchMetadata;
import network.piranya.platform.api.extension_models.execution.bots.ExecutionBot;
import network.piranya.platform.api.extension_models.execution.bots.Bot;
import network.piranya.platform.api.extension_models.execution.bots.BotMetadata;
import network.piranya.platform.api.extension_models.execution.bots.Command;
import network.piranya.platform.api.extension_models.execution.bots.CommandBot;
import network.piranya.platform.api.lang.None;
import network.piranya.platform.api.lang.Result;
import network.piranya.platform.api.lang.ResultHandler;
import network.piranya.platform.api.models.bots.BotRef;
import network.piranya.platform.api.models.bots.BotSpec;
import network.piranya.platform.api.models.metadata.BotTypeInfo;
import network.piranya.platform.api.models.metadata.CommandInfo;
import network.piranya.platform.api.models.metadata.ModuleInfo;
import network.piranya.platform.api.models.metadata.SearchInfo;
import network.piranya.platform.node.api.execution.bots.BotsRegistry;
import network.piranya.platform.node.api.local_infrastructure.storage.LocalStorage;
import network.piranya.platform.node.api.local_infrastructure.storage.PropertiesStore;
import network.piranya.platform.node.api.modules.ModuleMetadata;
import network.piranya.platform.node.utilities.CollectionUtils;
import network.piranya.platform.node.utilities.EventsSubscriptionSupport;
import network.piranya.platform.node.utilities.InputsValidator;
import network.piranya.platform.node.utilities.MetadataUtils;
import network.piranya.platform.node.utilities.ReflectionUtils;
import network.piranya.platform.node.utilities.StringUtils;

public class BotsRegistryImpl implements BotsRegistry {
	
	@Override
	@SuppressWarnings("unchecked")
	public <BotType extends Bot> void registerBotType(Class<BotType> botType, ModuleMetadata moduleMetadata) {
		Map<String, ExecutionBotInvokerImpl> invokers = new HashMap<>();
		Map<String, Method> commandMethods = new HashMap<>();
		List<Method> commandMethodsList = new ArrayList<>();
		for (Method method : botType.getDeclaredMethods()) {
			if (method.isAnnotationPresent(Command.class)) {
				if (!Modifier.isPublic(method.getModifiers())) {
					throw new PiranyaException(String.format("Operation method '%s' must be public", method.getName()));
				}
				
				if (ExecutionBot.class.isAssignableFrom(botType)) {
					if (!(method.getParameterTypes().length == 0
							|| (method.getParameterTypes().length == 1 && method.getParameterTypes()[0].isAssignableFrom(Parameters.class))
							|| (method.getParameterTypes().length == 1 && ExecutionBot.Context.class.isAssignableFrom(method.getParameterTypes()[0]))
							|| (method.getParameterTypes().length == 2
									&& method.getParameterTypes()[0].isAssignableFrom(Parameters.class) && ExecutionBot.Context.class.isAssignableFrom(method.getParameterTypes()[1])))) {
						throw new PiranyaException(String.format(
								"Bot Command Method '%s' allows for following arguments: (), (Parameters), (ExecutionBot.Context) or (Parameters, ExecutionBot.Context).", method.getName()));
					}
					if (!method.getReturnType().isAssignableFrom(Data.class) && !method.getReturnType().equals(Void.TYPE)) {
						throw new PiranyaException(String.format("Return type of method '%s' must be void or Data", method.getName()));
					}
				}
				
				commandMethods.put(method.getAnnotation(Command.class).id(), method);
				commandMethodsList.add(method);
			}
		}
		
		Set<String> features = new HashSet<>();
		boolean isSingleton = false;
		if (botType.isAnnotationPresent(BotMetadata.class)) {
			BotMetadata metadata = botType.getDeclaredAnnotation(BotMetadata.class);
			isSingleton = metadata.singleton();
			features.addAll(Arrays.asList(metadata.features()));
		}
		
		BotTypeInfo botTypeInfo = generateBotTypeInfo(botType, commandMethodsList, moduleMetadata);
		if (ExecutionBot.class.isAssignableFrom(botType)) {
			for (Map.Entry<String, Method> entry : commandMethods.entrySet()) {
				invokers.put(entry.getKey(), new ExecutionBotInvokerImpl(entry.getValue(), getCommandInfo(botTypeInfo, entry.getKey())));
			}
		}
		
		for (CommandInfo commandInfo : botTypeInfo.getCommands()) {
			features.addAll(Arrays.asList(commandInfo.getFeatures()));
		}
		
		botTypesMap().put(botType.getName(), new BotTypeRegistration((Class<Bot>)botType, invokers, commandMethods, isSingleton, features, botTypeInfo));
		
		eventsSubscriptionSupport.publish(new BotTypeEvent((Class<Bot>)botType, BotRegistryEvent.EventType.REGISTERED), true);
	}
	
	protected <BotType extends Bot> BotTypeInfo generateBotTypeInfo(Class<BotType> botType, List<Method> commandMethods, ModuleMetadata moduleMetadata) {
		String displayName = null;
		String description = "";
		String[] features = new String[0];
		String[] searchTags = new String[0];
		boolean isSingleton = false;
		if (botType.isAnnotationPresent(BotMetadata.class)) {
			BotMetadata metadata = botType.getDeclaredAnnotation(BotMetadata.class);
			features = metadata.features();
			isSingleton = metadata.singleton();
			displayName = metadata.displayName();
			description = metadata.description();
		}
		if (StringUtils.isEmpty(displayName)) {
			displayName = StringUtils.splitToCamelCase(botType.getSimpleName(), " ");
		}
		if (botType.isAnnotationPresent(SearchMetadata.class)) {
			searchTags = botType.getDeclaredAnnotation(SearchMetadata.class).tags();
		}
		
		return new BotTypeInfo(botType.getName(), ExecutionBot.class.isAssignableFrom(botType) ? BotTypeInfo.Type.EXECUTION : BotTypeInfo.Type.COMMAND,
				displayName, description,
				new ModuleInfo(moduleMetadata.moduleId(), moduleMetadata.version().toString(), moduleMetadata.displayName(), moduleMetadata.description(), moduleMetadata.uniqueModuleId()),
				isSingleton, features, sortBy(map(commandMethods, method -> mapCommand(method)), c -> c.getOrder()).toArray(new CommandInfo[0]),
				MetadataUtils.getInputsInfo(botType.isAnnotationPresent(InputMetadata.class) ? botType.getAnnotation(InputMetadata.class).inputs() : new Input[0]),
				new SearchInfo(searchTags));
	}
	
	protected CommandInfo mapCommand(Method method) {
		Command c = method.getAnnotation(Command.class);
		String[] commandTags = new String[0];
		if (method.isAnnotationPresent(SearchMetadata.class)) {
			commandTags = method.getDeclaredAnnotation(SearchMetadata.class).tags();
		}
		Input[] inputs = method.isAnnotationPresent(InputMetadata.class) ? method.getAnnotation(InputMetadata.class).inputs() : new Input[0];
		CommandInfo commandInfo = new CommandInfo(c.id(), StringUtils.hasText(c.displayName()) ? c.displayName() : c.id(),
				StringUtils.hasText(c.description()) ? c.description() : c.id(), MetadataUtils.getInputsInfo(inputs),
				method.getDeclaredAnnotation(Command.class).features(), new SearchInfo(commandTags), c.order());
		return commandInfo;
	}
	
	@Override
	public List<Class<Bot>> botTypes() {
		return map(botTypesMap().values(), r -> r.botType());
	}
	
	@Override
	public List<Class<Bot>> botTypesByFeature(String featureId) {
		return map(filter(botTypesMap().values(), r -> r.features().contains(featureId)), r -> r.botType());
	}
	
	@Override
	public String getBotTypeByFeature(String featureId, Parameters params) {
		String botClassName = botsForFeaturesMap().get(featureId);
		if (botClassName == null || !botTypesMap().containsKey(botClassName)) {
			botClassName = CollectionUtils.find(botTypesMap().values(), r -> r.features().contains(featureId)).orElseThrow(
					() -> new PiranyaException(String.format("Bot that implements feature '%s' does not exist", featureId))).botType().getName();
		}
		return botClassName;
	}
	
	@Override
	public Class<Bot> botType(String botTypeId) {
		return botTypeRegistration(botTypeId).botType();
	}
	
	@Override
	public BotTypeInfo botMetadata(String botTypeId) {
		return botTypeRegistration(botTypeId).info();
	}
	
	@Override
	public BotTypeInfo botMetadata(BotRef botRef) {
		BotRegistration reg = botsMap().get(botRef.botId());
		if (reg == null) {
			throw new BotNotFoundException(botRef.botId());
		}
		
		return botMetadata(reg.bot().getClass().getName());
	}
	
	@Override
	public List<BotTypeInfo> botTypeInfoList() {
		return map(botTypesMap().values(), r -> r.info);
	}
	
	@Override
	public ExecutionBot createBot(String botClassName, Parameters params, Function<ExecutionBot, String> botIdSupplier, Object registrar) {
		BotTypeRegistration reg = botTypeRegistration(botClassName);
		if (!ExecutionBot.class.isAssignableFrom(reg.botType())) {
			throw new PiranyaException(String.format("Bot Type '%s' is not an execution bot", botClassName));
		}
		@SuppressWarnings("unchecked")
		Class<ExecutionBot> botType = (Class<ExecutionBot>)(Class<?>)reg.botType();
		
		ExecutionBot bot = ReflectionUtils.createInstance(botType);
		ReflectionUtils.inject(bot, ExecutionBot.class, "ref", new BotRef(botIdSupplier.apply(bot)));
		ReflectionUtils.inject(bot, ExecutionBot.class, "params", params);
		
		if (botsMap().containsKey(bot.ref().botId())) {
			throw new PiranyaException(String.format("Bot '%s' is already registered", bot.ref().botId()));
		}
		botsMap().put(bot.ref().botId(), new BotRegistration(bot, registrar));
		
		return bot;
	}
	
	@Override
	public ExecutionBot get(BotRef botRef) {
		BotRegistration actor = botsMap().get(botRef.botId());
		if (actor == null) {
			throw new BotNotFoundException(String.format("Bot '%s' is not registered", botRef.botId()));
		}
		return actor.bot();
	}
	
	@Override
	public void deregister(BotRef botRef, Object registrar) {
		String actorId = botRef.botId();
		BotRegistration registration = botsMap().get(actorId);
		if (registration == null) {
			throw new BotNotFoundException(String.format("Bot '%s' is not registered", actorId));
		} else if (registrar != registration.registrar()) {
			throw new PiranyaException(String.format("Bot '%s' is not owned by this registrar", actorId));
		}
		
		botsMap().remove(actorId);
	}
	
	@Override
	public Collection<ExecutionBot> botsList() {
		return map(botsMap().values(), r -> r.bot());
	}
	
	@Override
	public ExecutionBotInvoker executionBotInvoker(BotRef botRef, String commandId) {
		ExecutionBot bot = get(botRef);
		BotTypeRegistration agentTypeRegistration = botTypesMap().get(bot.getClass().getName());
		ExecutionBotInvokerImpl invoker = agentTypeRegistration.invokers().get(commandId);
		if (invoker == null) {
			throw new PiranyaException(String.format("Command '%s' is not defined in Agent '%s'", commandId, bot.getClass().getName()));
		}
		return invoker;
	}
	
	@Override
	public CommandBotInvoker commandBotInvoker(BotSpec spec, String commandId) {
		String botClassName = spec.botTypeId().orElseGet(() -> getBotTypeByFeature(spec.featureId().get(), Parameters.EMPTY));
		
		BotTypeRegistration reg = botTypeRegistration(botClassName);
		if (!CommandBot.class.isAssignableFrom(reg.botType())) {
			throw new PiranyaException(String.format("Bot Type '%s' is not command bot", botClassName));
		}
		Method method = reg.commandMethods().get(commandId);
		if (method == null) {
			throw new PiranyaException(String.format("Bot Type '%s' does not have command '%s'", botClassName, commandId));
		}
		@SuppressWarnings("unchecked")
		Class<CommandBot> botType = (Class<CommandBot>)(Class<?>)reg.botType();
		
		CommandBot bot = ReflectionUtils.createInstance(botType);
		
		return new CommandBotInvokerImpl(bot, method, getCommandInfo(reg.info(), commandId));
	}
	
	@Override
	public void prioritizeBotForFeature(String botClassName, String featureId) {
		BotTypeRegistration registration = botTypesMap().get(botClassName);
		if (registration != null && registration.features().contains(featureId)) {
			botsForFeaturesMap().put(featureId, botClassName);
			db().updateProperty(BOTS_FOR_FEATURES_MAP_DB_ENTRY, botsForFeaturesMap());
		}
	}
	
	public void subscribe(Consumer<BotRegistryEvent> subscriber) {
		eventsSubscriptionSupport.subscribe(subscriber);
	}
	
	public void unsubscribe(Consumer<BotRegistryEvent> subscriber) {
		eventsSubscriptionSupport.unsubscribe(subscriber);
	}
	
	private final EventsSubscriptionSupport<BotRegistryEvent> eventsSubscriptionSupport = new EventsSubscriptionSupport<>();
	
	
	protected BotTypeRegistration botTypeRegistration(String botTypeId) {
		BotTypeRegistration reg = botTypesMap().get(botTypeId);
		if (reg == null) {
			throw new PiranyaException(String.format("Bot Type '%s' is not registered", botTypeId));
		}
		return reg;
	}
	
	protected CommandInfo getCommandInfo(BotTypeInfo botInfo, String commandId) {
		CommandInfo commandInfo = null;
		for (CommandInfo c : botInfo.getCommands()) {
			if (c.getCommandId().equals(commandId)) {
				commandInfo = c;
				break;
			}
		}
		if (commandInfo == null) {
			throw new PiranyaException(String.format("Command '%s' not found in bot '%s'", commandId, botInfo.getBotTypeId()));
		}
		return commandInfo;
	}
	
	
	public BotsRegistryImpl(LocalStorage localStorage) {
		this.db = localStorage.properties("bots_config");
		
		botsForFeaturesMap().putAll(db().getMap(BOTS_FOR_FEATURES_MAP_DB_ENTRY));
	}
	
	private final PropertiesStore db;
	protected PropertiesStore db() {
		return db;
	}
	
	private final ConcurrentMap<String, BotTypeRegistration> botTypesMap = new ConcurrentHashMap<>();
	protected ConcurrentMap<String, BotTypeRegistration> botTypesMap() {
		return botTypesMap;
	}
	
	private final ConcurrentMap<String, BotRegistration> botsMap = new ConcurrentHashMap<>();
	protected ConcurrentMap<String, BotRegistration> botsMap() {
		return botsMap;
	}
	
	private final ConcurrentMap<String, String> botsForFeaturesMap = new ConcurrentHashMap<>();
	protected ConcurrentMap<String, String> botsForFeaturesMap() {
		return botsForFeaturesMap;
	}
	
	private final InputsValidator inputsValidator = new InputsValidator();
	protected InputsValidator inputsValidator() { return inputsValidator; }
	
	
	public static class BotRegistration {
		
		private final ExecutionBot agent;
		public ExecutionBot bot() {
			return agent;
		}
		
		private final Object registrar;
		public Object registrar() {
			return registrar;
		}
		
		public BotRegistration(ExecutionBot actor, Object registrar) {
			this.agent = actor;
			this.registrar = registrar;
		}
	}
	
	protected class BotTypeRegistration {
		
		public BotTypeRegistration(Class<Bot> botType, Map<String, ExecutionBotInvokerImpl> invokers, Map<String, Method> commandMethods,
				boolean isSingleton, Set<String> features, BotTypeInfo info) {
			this.botType = botType;
			this.invokers = invokers;
			this.commandMethods = commandMethods;
			this.isSingleton = isSingleton;
			this.features = features;
			this.info = info;
		}
		
		private final Class<Bot> botType;
		public Class<Bot> botType() { return botType; }
		
		private final boolean isSingleton;
		public boolean isSingleton() { return isSingleton; }
		
		private final Map<String, ExecutionBotInvokerImpl> invokers;
		public Map<String, ExecutionBotInvokerImpl> invokers() { return invokers; }
		
		private final Map<String, Method> commandMethods;
		public Map<String, Method> commandMethods() { return commandMethods; }
		
		private final Set<String> features;
		public Set<String> features() { return features; }
		
		private final BotTypeInfo info;
		public BotTypeInfo info() { return info; }
	}
	
	protected class ExecutionBotInvokerImpl implements ExecutionBotInvoker {
		
		@Override
		public void invoke(ExecutionBot bot, Parameters params, ExecutionBot.Context executionContext, Consumer<Result<Data>> resultHandler) {
			try {
				inputsValidator().validate(params, method.isAnnotationPresent(InputMetadata.class) ? method.getAnnotation(InputMetadata.class).inputs() : new Input[0]);
				
				Data data = null;
				if (method.getParameterTypes().length == 0) {
					data = (Data)method.invoke(bot);
				} else if (method.getParameterTypes().length == 2) {
					data = (Data)method.invoke(bot, params, executionContext);
				} else if (method.getParameterTypes().length == 1 && method.getParameterTypes()[0].isAssignableFrom(Parameters.class)) {
					data = (Data)method.invoke(bot, params);
				} else if (method.getParameterTypes().length == 1 && ExecutionBot.Context.class.isAssignableFrom(method.getParameterTypes()[0])) {
					data = (Data)method.invoke(bot, executionContext);
				} else {
					throw new PiranyaException(String.format("Failed to find suitable command method for '%s' method '%s'", bot.ref(), method.getName()));
				}
				resultHandler.accept(new Result<>(data != null ? data : new Data()));
			} catch (ExtensionException ex) {
				ex.printStackTrace();
				resultHandler.accept(new Result<>(ex));
			} catch (Throwable ex) {
				ex.printStackTrace();
				throw new PiranyaException(String.format("An error occurred while invoking '%s.%s': %s",
						method.getDeclaringClass().getName(), method.getName(), ex.getMessage()), ex);
			}
		}
		
		@Override
		public CommandInfo commandInfo() {
			return commandInfo;
		}
		
		
		public ExecutionBotInvokerImpl(Method method, CommandInfo commandInfo) {
			this.method = method;
			this.commandInfo = commandInfo;
			
			for (Class<?> paramType : method.getParameterTypes()) {
				if (paramType.equals(ExecutionBot.DetachedContext.class)) {
					this.isDetachedContext = true;
				}
			}
		}
		
		private final Method method;
		private final CommandInfo commandInfo;
		private boolean isDetachedContext = false;
		
		@Override
		public boolean isDetachedContext() {
			return isDetachedContext;
		}
	}
	
	protected class CommandBotInvokerImpl implements CommandBotInvoker {
		
		@Override
		public void invoke(Parameters params, ResultHandler<Object> resultHandler, Supplier<ManagerialExtensionContext> context) {
			try {
				inputsValidator().validate(params, method.isAnnotationPresent(InputMetadata.class) ? method.getAnnotation(InputMetadata.class).inputs() : new Input[0]);
				
				if (this.context == null) {
					this.context = context.get();
					ReflectionUtils.inject(bot, CommandBot.class, "context", this.context);
				}
				
				Object result = null;
				if (method.getParameterTypes().length == 0) {
					result = method.invoke(bot);
					if (result != null) {
						resultHandler.accept(new Result<>(result));
					} else {
						resultHandler.accept(new Result<>(None.VALUE));
					}
				} else if (method.getParameterTypes().length == 1) {
					if (Parameters.class.isAssignableFrom(method.getParameterTypes()[0])) {
						result = method.invoke(bot, params);
						if (result != null) {
							resultHandler.accept(new Result<>(result));
						} else {
							resultHandler.accept(new Result<>(None.VALUE));
						}
					} else {
						method.invoke(bot, resultHandler);
					}
				} else if (method.getParameterTypes().length == 2) {
					method.invoke(bot, params, resultHandler);
				} else {
					throw new PiranyaException(String.format("Failed to find suitable command method for '%s' method '%s'", bot.getClass().getName(), method.getName()));
				}
			} catch (InvocationTargetException ex) {
				if (ex.getCause() == null || (ExtensionException.class.isInstance(ex.getCause()) || InvalidParameterException.class.isInstance(ex.getCause()))) {
					LOG.warn(String.format("Internal error in command method '%s' of bot '%s': %s", method.getName(), bot.getClass().getName(), ex.getMessage()), ex);
				}
				resultHandler.accept(new Result<>((Exception)ex.getCause()));
			} catch (Throwable ex) {
				LOG.warn(String.format("Failed to invoker method '%s' of bot '%s': %s", method.getName(), bot.getClass().getName(), ex.getMessage()), ex);
				resultHandler.accept(new Result<>((Exception)ex.getCause()));
			}
		}
		
		@Override
		public CommandInfo commandInfo() {
			return commandInfo;
		}
		
		
		public CommandBotInvokerImpl(CommandBot bot, Method method, CommandInfo commandInfo) {
			this.bot = bot;
			this.method = method;
			this.commandInfo = commandInfo;
		}
		
		private final CommandBot bot;
		private final Method method;
		private final CommandInfo commandInfo;
		private ManagerialExtensionContext context = null;
		
	}
	
	
	private static final String BOTS_FOR_FEATURES_MAP_DB_ENTRY = "botsForFeaturesMap";
	
	private static final Logger LOG = LoggerFactory.getLogger(BotsRegistryImpl.class);
	
}
