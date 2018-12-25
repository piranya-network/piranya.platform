package network.piranya.platform.node.core.execution.engine.bots;

import static network.piranya.platform.node.utilities.CollectionUtils.*;
import static network.piranya.platform.node.utilities.ReflectionUtils.*;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import network.piranya.platform.api.exceptions.PiranyaException;
import network.piranya.platform.api.extension_models.Data;
import network.piranya.platform.api.extension_models.DataBuilder;
import network.piranya.platform.api.extension_models.ExtensionContext;
import network.piranya.platform.api.extension_models.Parameters;
import network.piranya.platform.api.extension_models.execution.bots.ExecutionBot;
import network.piranya.platform.api.extension_models.execution.bots.BotEvent;
import network.piranya.platform.api.extension_models.execution.bots.EventProcessor;
import network.piranya.platform.api.extension_models.execution.sandbox.SandboxConfiguration;
import network.piranya.platform.api.extension_models.execution.bots.ExecutionBot.DetachedContext;
import network.piranya.platform.api.extension_models.execution.bots.ExecutionBot.ForkBotParameters;
import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.api.lang.ReplyHandler;
import network.piranya.platform.api.lang.Result;
import network.piranya.platform.api.models.analytics.Analytics;
import network.piranya.platform.api.models.bots.BotRef;
import network.piranya.platform.api.models.bots.BotView;
import network.piranya.platform.api.models.bots.BotsAdmin;
import network.piranya.platform.api.models.execution.ExecutionEngineReader;
import network.piranya.platform.api.models.execution.sandbox.Sandbox;
import network.piranya.platform.api.models.info.MarketInfoProvider;
import network.piranya.platform.api.models.infrastructure.SerializationServices;
import network.piranya.platform.api.models.infrastructure.net.NetServices;
import network.piranya.platform.api.models.infrastructure.storage.StorageServices;
import network.piranya.platform.api.models.log.ActivityLogStore;
import network.piranya.platform.api.models.log.BotRemovedLog;
import network.piranya.platform.api.models.metadata.BotTypeInfo;
import network.piranya.platform.api.models.search.SearchService;
import network.piranya.platform.api.models.trading.ExecutionEvent;
import network.piranya.platform.api.models.trading.TradingState;
import network.piranya.platform.api.models.trading.liquidity.LiquidityProvidersListing;
import network.piranya.platform.node.api.execution.ExecutionManager;
import network.piranya.platform.node.api.execution.bots.BotsRegistry;
import network.piranya.platform.node.api.execution.bots.BotsRegistry.ExecutionBotInvoker;
import network.piranya.platform.node.api.execution.commands.ExecutionCommand;
import network.piranya.platform.node.api.local_infrastructure.concurrency.Executor;
import network.piranya.platform.node.core.execution.accessor.ExecutionEngineReaderImpl;
import network.piranya.platform.node.core.execution.engine.ActivityLogger;
import network.piranya.platform.node.core.execution.engine.LpExectuionBranch;
import network.piranya.platform.node.core.execution.engine.TradingRepository;
import network.piranya.platform.node.core.execution.engine.TradingStateImpl;
import network.piranya.platform.node.core.execution.engine.activity_log.BotAbortedLog;
import network.piranya.platform.node.core.execution.engine.activity_log.BotEsLog;
import network.piranya.platform.node.core.execution.engine.activity_log.EsLog;
import network.piranya.platform.node.core.execution.engine.activity_log.BotCommandedLog;
import network.piranya.platform.node.core.execution.engine.activity_log.BotCreatedLog;
import network.piranya.platform.node.core.execution.engine.infrastructure.ExtensionExecutor;
import network.piranya.platform.node.core.execution.engine.infrastructure.net.NetServicesImpl;
import network.piranya.platform.node.core.execution.engine.infrastructure.serialization.SerializationServicesImpl;
import network.piranya.platform.node.core.execution.engine.infrastructure.storage.StorageOperator;
import network.piranya.platform.node.core.execution.engine.infrastructure.storage.StorageServicesImpl;
import network.piranya.platform.node.core.execution.engine.sandbox.SandboxEngine;
import network.piranya.platform.node.core.execution.engine.sandbox.SandboxImpl;
import network.piranya.platform.node.utilities.DisposableSupport;
import network.piranya.platform.node.utilities.TimeService;

public class BotsOperator {
	
	public BotView createBot(String botClassName, Parameters params, Optional<String> actorId, Optional<Consumer<BotEvent>> botEventsSubscriber) {
		String actualBotId = actorId.orElse(nextBotId());
		return createBot(botClassName, params, actualBotId, botEventsSubscriber);
	}
	
	public BotView abortBot(BotRef botRef) {
		RunningBot runningBot = runningBots().get(botRef.botId());
		if (runningBot == null) {
			throw new PiranyaException(String.format("Agent '%s' does not exist", botRef.botId()));
		}
		
		try (ExecutionContextImpl executionContext = new ExecutionContextImpl(runningBot)) {
			runningBot.bot().onAbort(executionContext);
		}
		onBotEvent(runningBot.bot().ref(), new BotEvent(runningBot.bot().ref(), runningBot.bot().getClass().getName(), BotEvent.ABORTED_EVENT_TYPE_ID, new DataBuilder().build()));
		
		BotView view = runningBot.bot().view();
		
		disposeOfBot(runningBot);
		
		if (!isReplaying()) {
			activityLogger().log(new BotRemovedLog(runningBot.bot().ref(), TimeService.now()));
		}
		
		return view;
	}
	
	/**
	 * @return true if command is transactional. false is for a detached context.
	 */
	public boolean commandBot(BotRef botRef, String commandId, Parameters params, Consumer<Result<Data>> resultHandler, Runnable saveExecution) {
		RunningBot runningBot = runningBots().get(botRef.botId());
		if (runningBot == null) {
			throw new PiranyaException(String.format("Bot '%s' does not exist", botRef.botId()));
		}
		
		ExecutionBotInvoker commandInvoker = botsRegistry().executionBotInvoker(botRef, commandId);
		if (!commandInvoker.isDetachedContext()) {
			saveExecution.run();
			try (ExecutionContextImpl executionContext = new ExecutionContextImpl(runningBot)) {
				commandInvoker.invoke(runningBot.bot(), params, executionContext, resultHandler);
				return true;
			} catch (Exception ex) {
				resultHandler.accept(new Result<>(ex));
				return false;
			}
		} else {
			detachedThreadsExecutor().execute(() -> commandInvoker.invoke(runningBot.bot(), params,
					new DetachedContextImpl(Optional.of(runningBot), botsRegistry().botMetadata(runningBot.bot().ref()).getModuleInfo().getUniqueModuleId()), resultHandler));
			return false;
		}
	}
	
	public void accept(BotEsLog log) {
		if (log instanceof BotCreatedLog) {
			BotCreatedLog l = (BotCreatedLog)log;
			currentBotId = Long.parseLong(l.botId().substring(l.botId().lastIndexOf(':') + 1));
			try { createBot(l.actorClassName(), l.params(), l.botId(), Optional.empty()); }
			catch (Throwable ex) { }
		} else if (log instanceof BotAbortedLog) {
			BotAbortedLog l = (BotAbortedLog)log;
			abortBot(new BotRef(l.botId()));
		} else if (log instanceof BotCommandedLog) {
			BotCommandedLog l = (BotCommandedLog)log;
			commandBot(new BotRef(l.botId()), l.commandId(), l.params(), result -> {}, () -> {});
		} else {
			throw new PiranyaException(String.format("Activity Log of type '%s' not supported", log.getClass().getName()));
		}
	}
	
	public ExtensionContext createDetachedExtensionContext(String moduleId) {
		return new DetachedContextImpl(Optional.empty(), moduleId);
	}
	
	
	@SuppressWarnings("unchecked")
	protected BotView createBot(String botClassName, Parameters params, String botId, Optional<Consumer<BotEvent>> botEventsSubscriber) {
		ExecutionBot bot = botsRegistry().createBot(botClassName, params, a -> botId, this);
		BotTypeInfo botTypeInfo = botsRegistry().botMetadata(botClassName);
		
		RunningBot runningBot = new RunningBot(bot);
		botEventsSubscriber.ifPresent(subscriber -> runningBot.eventSubscribers().add(subscriber));
		runningBots().put(bot.ref().botId(), runningBot);
		
		inject(bot, ExecutionBot.class, "eventsListener", newBotEventsListener(bot));
		inject(bot, ExecutionBot.class, "subscribeOperation", runningBot.subscribeOperation());
		inject(bot, ExecutionBot.class, "unsubscribeOperation", runningBot.unsubscribeOperation());
		
		ExecutionContextImpl initialContext = new ExecutionContextImpl(runningBot);
		for (Method method : findMethodsByAnnotation(bot.getClass(), EventProcessor.class)) {
			initialContext.subscribe((Class<ExecutionEvent>)method.getParameterTypes()[0]);
		}
		initialContext.close();
		
		if (!isReplaying()) {
			logConsumer().accept(new BotCreatedLog(botClassName, params, botId));
			activityLogger().log(new network.piranya.platform.api.models.log.BotCreatedLog(new BotRef(bot.ref().botId()), botClassName, botTypeInfo.getFeatures(), TimeService.now()));
		}
		
		try (ExecutionContextImpl executionContext = new ExecutionContextImpl(runningBot)) {
			bot.onStart(executionContext);
		} catch (Exception ex) {
			disposeOfBot(runningBot);
			throw ex;
		}
		
		onBotEvent(runningBot.bot().ref(), new BotEvent(runningBot.bot().ref(), runningBot.bot().getClass().getName(), BotEvent.STARTED_EVENT_TYPE_ID, new DataBuilder().build()));
		
		return bot.view();
	}
	
	protected void finishBot(BotRef agentRef, Optional<Exception> error) {
		// TODO publish event
		//if (isLog) logActivity(new ActorFinishedLog(actor.ref().actorId(), Optional.of(String.format("Error: %s: %s", error.getClass().getSimpleName(), error.getMessage()))));
		RunningBot runningBot = runningBots().get(agentRef.botId());
		
		try (ExecutionContextImpl executionContext = new ExecutionContextImpl(runningBot)) {
			runningBot.bot().onFinish(executionContext);
		}
		onBotEvent(runningBot.bot().ref(), new BotEvent(runningBot.bot().ref(), runningBot.bot().getClass().getName(), BotEvent.FINISHED_EVENT_TYPE_ID, new DataBuilder().build()));
		
		disposeOfBot(runningBot);
		
		if (!isReplaying()) {
			activityLogger().log(new BotRemovedLog(runningBot.bot().ref(), TimeService.now()));
		}
	}
	
	protected void disposeOfBot(RunningBot runningBot) {
		foreach(botEventSubscriptionsMap().values(), map -> {
			foreach(filter(map.keySet(), s -> s.runningBot() == runningBot), s -> map.remove(s));
		});
		
		runningBot.dispose();
		botsRegistry().deregister(runningBot.bot().ref(), this);
		runningBots().remove(runningBot.bot().ref().botId());
	}
	
	protected String nextBotId() {
		return String.format("%s:%s", botsPrefix(), ++currentBotId);
	}
	private long currentBotId = 0;
	
	
	private final Consumer<BotEvent> newBotEventsListener(ExecutionBot bot) {
		return event -> onBotEvent(bot.ref(), event);
	}
	
	protected void onBotEvent(BotRef botRef, BotEvent event) {
		RunningBot runningBot = runningBots().get(botRef.botId());
		if (runningBot != null) {
			for (Consumer<BotEvent> subscriber : runningBot.eventSubscribers()) {
				try { subscriber.accept(event); }
				catch (Throwable ex) { LOG.warn(String.format("Failed to invoke subscriber: %s", ex.getMessage()), ex); }
			}
		}
		
		acceptExecutionEvent(event);
		botEventsConsumer().accept(event);
	}
	
	
	public boolean acceptExecutionEvent(ExecutionEvent event) {
		boolean isHandled = false;
		ExecutionEvent processingEvent = event;
		
		ConcurrentMap<BotEventSubscription, Boolean> subscriptions = botEventSubscriptionsMap().get(processingEvent.getClass());
		if (subscriptions != null) {
			for (BotEventSubscription subscription : subscriptions.keySet()) {
				if (subscription.predicate().test(event)) {
					if (subscription.isDemandsOnReplay()) {
						isHandled = true;
					}
					
					try { subscription.consumer().accept(event); }
					catch (Throwable ex) { LOG.warn(String.format("Failed to invoke subscription: %s", ex.getMessage()), ex); }
				}
			}
		}
		return isHandled;
	}
	
	protected void subscribeBotToEvent(BotEventSubscription subscription) {
		ConcurrentMap<BotEventSubscription, Boolean> m = botEventSubscriptionsMap().get(subscription.eventType());
		if (m == null) {
			m = new ConcurrentHashMap<>();
			botEventSubscriptionsMap().putIfAbsent(subscription.eventType(), m);
			m = botEventSubscriptionsMap().get(subscription.eventType());
		}
		final ConcurrentMap<BotEventSubscription, Boolean> map = m;
		
		/// if general listener exists with no predicate, then it's default from annotation processing. Remove to avoid duplicate handling.
		find(map.keySet(), s -> s.runningBot() == subscription.runningBot() && !s.hasPredicate()).ifPresent(s -> map.remove(s));
		
		map.put(subscription, true);
	}
	
	private final ConcurrentMap<Class<ExecutionEvent>, ConcurrentMap<BotEventSubscription, Boolean>> botEventSubscriptionsMap = new ConcurrentHashMap<>();
	protected ConcurrentMap<Class<ExecutionEvent>, ConcurrentMap<BotEventSubscription, Boolean>> botEventSubscriptionsMap() { return botEventSubscriptionsMap; }
	
	
	public BotsOperator(String botsPrefix, BotsRegistry botsRegistry, StorageOperator storageOperator, TradingRepository tradingRepository, Executor detachedThreadsExecutor,
			Function<String, LpExectuionBranch> lpExectuionBranchesProvider, Consumer<EsLog> logConsumer, Consumer<BotEvent> botEventsConsumer, Consumer<ExecutionCommand> executionCommandProcessor,
			Supplier<Analytics> analyticsFactory, Function<SandboxConfiguration, SandboxEngine> sandboxEngineFactory, SearchService searchService,
			MarketInfoProvider marketInfoProvider, ActivityLogger activityLogger, ExecutionManager executionManager) {
		this.botsPrefix = botsPrefix;
		this.botsRegistry = botsRegistry;
		this.storageOperator = storageOperator;
		this.tradingRepository = tradingRepository;
		this.detachedThreadsExecutor = detachedThreadsExecutor;
		this.lpExectuionBranchesProvider = lpExectuionBranchesProvider;
		this.logConsumer = logConsumer;
		this.botEventsConsumer = botEventsConsumer;
		this.executionCommandProcessor = executionCommandProcessor;
		this.analyticsFactory = analyticsFactory;
		this.sandboxEngineFactory = sandboxEngineFactory;
		this.searchService = searchService;
		this.marketInfoProvider = marketInfoProvider;
		this.activityLogger = activityLogger;
		this.executionManager = executionManager;
	}
	
	private final String botsPrefix;
	protected String botsPrefix() { return botsPrefix; }
	
	private final BotsRegistry botsRegistry;
	protected BotsRegistry botsRegistry() { return botsRegistry; }
	
	private final StorageOperator storageOperator;
	protected StorageOperator storageOperator() { return storageOperator; }
	
	private final TradingRepository tradingRepository;
	protected TradingRepository tradingRepository() { return tradingRepository; }
	
	private final Executor detachedThreadsExecutor;
	protected Executor detachedThreadsExecutor() { return detachedThreadsExecutor; }
	
	private final Function<String, LpExectuionBranch> lpExectuionBranchesProvider;
	protected Function<String, LpExectuionBranch> lpExectuionBranchesProvider() { return lpExectuionBranchesProvider; }
	
	private final Consumer<EsLog> logConsumer;
	protected Consumer<EsLog> logConsumer() { return logConsumer; }
	
	private final Consumer<BotEvent> botEventsConsumer;
	protected Consumer<BotEvent> botEventsConsumer() { return botEventsConsumer; }
	
	private final Consumer<ExecutionCommand> executionCommandProcessor;
	protected Consumer<ExecutionCommand> executionCommandProcessor() { return executionCommandProcessor; }
	
	private final Supplier<Analytics> analyticsFactory;
	protected Supplier<Analytics> analyticsFactory() { return analyticsFactory; }
	
	private final ActivityLogger activityLogger;
	protected ActivityLogger activityLogger() { return activityLogger; }
	
	private final ExecutionManager executionManager;
	protected ExecutionManager executionManager() { return executionManager; }
	
	private final ConcurrentMap<String, RunningBot> runningBots = new ConcurrentHashMap<>();
	protected ConcurrentMap<String, RunningBot> runningBots() { return runningBots; }
	
	private final Function<SandboxConfiguration, SandboxEngine> sandboxEngineFactory;
	protected Function<SandboxConfiguration, SandboxEngine> sandboxEngineFactory() { return sandboxEngineFactory; }
	
	private final SearchService searchService;
	protected SearchService searchService() { return searchService; }
	
	private final MarketInfoProvider marketInfoProvider;
	
	private boolean isReplaying = false;
	protected boolean isReplaying() { return isReplaying; }
	protected void setReplaying(boolean isReplaying) { this.isReplaying = isReplaying; }
	
	public void startReplay() {
		setReplaying(true);
	}
	
	public void finishReplay() {
		setReplaying(false);
	}
	
	
	protected class ExecutionContextImpl extends AbstractBotContext implements ExecutionBot.ExecutionContext {
		
		@Override
		public TradingState tradingState() {
			return new TradingStateImpl(tradingRepository());
		}
		
		@Override
		public ReplyHandler<BotRef> forkBot(ForkBotParameters params) {
			checkIfDisposed();
			
			// TODO
			throw new RuntimeException("NotImplemented");
		}
		
		@Override
		public LiquidityProvidersListing<ExecutionBot.ExecutionContext> liquidityProviders() {
			checkIfDisposed();
			
			return liquidityProvidersListing;
		}
		
		protected ExecutionContextImpl newExecutionContext() {
			return new ExecutionContextImpl(bot());
		}
		
		public ExecutionContextImpl(RunningBot bot) {
			super(bot, botsRegistry(), BotsOperator.this::subscribeBotToEvent);
			this.liquidityProvidersListing = new LiquidityProvidersListingImpl(lpExectuionBranchesProvider(), bot(), this, () -> new ExecutionContextImpl(bot));
			this.uniqueModuleId = botsRegistry().botMetadata(bot().bot().ref()).getModuleInfo().getUniqueModuleId();
			init(BotsOperator.this::finishBot);
		}
		
		private final String uniqueModuleId;
		
		private final LiquidityProvidersListingImpl liquidityProvidersListing;
		
		@Override
		public <SandboxConfigurationClass extends SandboxConfiguration> Sandbox createSandbox(SandboxConfigurationClass sandboxConfig) {
			checkIfDisposed();
			
			return new SandboxImpl(sandboxConfig, sandboxEngineFactory());
		}
		
		@Override
		public StorageServices storage() {
			if (storage == null) {
				storage = new StorageServicesImpl(Optional.of(bot()), storageOperator(), uniqueModuleId);
			}
			return storage;
		}
		private StorageServicesImpl storage;
		
		@Override
		public SerializationServices serialization() {
			return serializationServices;
		}
		private final SerializationServicesImpl serializationServices = new SerializationServicesImpl();

		@Override
		public void dispose() {
			super.dispose();
			
			if (storage != null) {
				storage.dispose();
			}
		}

		@Override
		public DetachedContext acquireDetachedContext() {
			return new DetachedContextImpl(Optional.of(bot()), uniqueModuleId);
		}
	}
	
	protected class DetachedContextImpl implements DetachedContext, AutoCloseable {
		
		@Override
		public BotsAdmin bots() {
			disposable.checkIfDisposed();
			return botsAdmin;
		}
		
		@Override
		public ExecutionEngineReader executionEngine() {
			disposable.checkIfDisposed();
			return executionEngineReader;
		}
		
		@Override
		public Analytics analytics() {
			if (analytics == null) {
				analytics = analyticsFactory().get();
			}
			return analytics;
		}
		private Analytics analytics;
		
		@Override
		public StorageServices storage() {
			disposable.checkIfDisposed();
			if (storage == null) {
				storage = new StorageServicesImpl(bot(), storageOperator(), uniqueModuleId);
			}
			return storage;
		}
		private StorageServicesImpl storage;
		
		@Override
		public NetServices net() {
			disposable.checkIfDisposed();
			if (netServices == null) {
				netServices = new NetServicesImpl(detachedThreadsExecutor());
			}
			return netServices;
		}
		private NetServicesImpl netServices;
		
		@Override
		public network.piranya.platform.api.models.infrastructure.Executor executor() {
			disposable.checkIfDisposed();
			if (executor == null) {
				executor = new ExtensionExecutor(detachedThreadsExecutor());
			}
			return executor;
		}
		private ExtensionExecutor executor;
		
		@Override
		public <SandboxConfigurationClass extends SandboxConfiguration> Sandbox createSandbox(SandboxConfigurationClass sandboxConfig) {
			disposable.checkIfDisposed();
			
			return new SandboxImpl(sandboxConfig, sandboxEngineFactory());
		}
		
		@Override
		public SearchService search() {
			disposable.checkIfDisposed();
			
			return searchService();
		}
		
		@Override
		public ActivityLogStore activityLog() {
			disposable.checkIfDisposed();
			if (activityLogStore == null) {
				activityLogStore = new ActivityLogStoreImpl(activityLogger());
			}
			return activityLogStore;
		}
		private ActivityLogStoreImpl activityLogStore;
		
		@Override
		public MarketInfoProvider marketInfo() {
			return marketInfoProvider;
		}
		
		@Override
		public SerializationServices serialization() {
			return serializationServices;
		}
		private final SerializationServicesImpl serializationServices = new SerializationServicesImpl();
		
		@Override
		public void dispose() {
			disposable.markDisposed();
			
			executionEngineReader.dispose();
			
			if (storage != null) {
				storage.dispose();
			}
			
			if (netServices != null) {
				netServices.dispose();
			}
			
			if (activityLogStore != null) {
				activityLogStore.dispose();
			}
		}

		@Override
		public void close() {
			if (!disposable.isDisposed()) {
				dispose();
			}
		}
		
		
		public DetachedContextImpl(Optional<RunningBot> bot, String uniqueModuleId) {
			this.bot = bot;
			this.uniqueModuleId = uniqueModuleId;
			this.botsAdmin = new BotsAdminImpl(executionCommandProcessor(), botsRegistry());
			this.executionEngineReader = new ExecutionEngineReaderImpl(executionManager, botsRegistry());
		}
		
		private final Optional<RunningBot> bot;
		public Optional<RunningBot> bot() { return bot; }
		
		private String uniqueModuleId;
		
		private final BotsAdminImpl botsAdmin;
		private final ExecutionEngineReaderImpl executionEngineReader;
		
		private final DisposableSupport disposable = new DisposableSupport(this);
		
	}
	
	
	private static final Logger LOG = LoggerFactory.getLogger(BotsOperator.class);
	
}
