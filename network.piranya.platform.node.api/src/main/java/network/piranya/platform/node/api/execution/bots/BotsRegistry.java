package network.piranya.platform.node.api.execution.bots;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import network.piranya.platform.api.extension_models.Data;
import network.piranya.platform.api.extension_models.ManagerialExtensionContext;
import network.piranya.platform.api.extension_models.Parameters;
import network.piranya.platform.api.extension_models.execution.bots.Bot;
import network.piranya.platform.api.extension_models.execution.bots.ExecutionBot;
import network.piranya.platform.api.lang.Result;
import network.piranya.platform.api.lang.ResultHandler;
import network.piranya.platform.api.models.bots.BotRef;
import network.piranya.platform.api.models.bots.BotSpec;
import network.piranya.platform.api.models.metadata.BotTypeInfo;
import network.piranya.platform.api.models.metadata.CommandInfo;
import network.piranya.platform.node.api.modules.ModuleMetadata;

public interface BotsRegistry {
	
	<BotType extends Bot> void registerBotType(Class<BotType> botType, ModuleMetadata moduleMetadata);
	
	ExecutionBot createBot(String botClassName, Parameters params, Function<ExecutionBot, String> botIdSupplier, Object registrar);
	
	String getBotTypeByFeature(String featureId, Parameters params);
	
	Class<Bot> botType(String botTypeId);
	
	BotTypeInfo botMetadata(BotRef botRef);
	
	BotTypeInfo botMetadata(String botTypeId);
	
	List<BotTypeInfo> botTypeInfoList();
	
	ExecutionBot get(BotRef botRef);
	
	void deregister(BotRef botRef, Object registrar);
	
	List<Class<Bot>> botTypes();
	
	List<Class<Bot>> botTypesByFeature(String featureId);
	
	Collection<ExecutionBot> botsList();
	
	ExecutionBotInvoker executionBotInvoker(BotRef botRef, String commandId);
	CommandBotInvoker commandBotInvoker(BotSpec spec, String commandId);
	
	void prioritizeBotForFeature(String botClassName, String featureId);
	
	void subscribe(Consumer<BotRegistryEvent> subscriber);
	void unsubscribe(Consumer<BotRegistryEvent> subscriber);
	
	
	public interface ExecutionBotInvoker {
		
		void invoke(ExecutionBot bot, Parameters params, ExecutionBot.Context executionContext, Consumer<Result<Data>> resultHandler);
		
		boolean isDetachedContext();
		
		CommandInfo commandInfo();
		
	}
	
	public interface CommandBotInvoker {
		
		void invoke(Parameters params, ResultHandler<Object> resultHandler, Supplier<ManagerialExtensionContext> context);
		
		CommandInfo commandInfo();
		
	}
	
	
	public interface BotRegistryEvent {
		
		public static enum EventType { REGISTERED, DEREGISTERED }
		
	}
	
	public class BotTypeEvent implements BotRegistryEvent {
		
		private final Class<Bot> botType;
		public Class<Bot> botType() { return botType; }
		
		private final EventType eventType;
		public EventType eventType() { return eventType; }
		
		public BotTypeEvent(Class<Bot> botType, EventType eventType) {
			this.botType = botType;
			this.eventType = eventType;
		}
	}
	
}
