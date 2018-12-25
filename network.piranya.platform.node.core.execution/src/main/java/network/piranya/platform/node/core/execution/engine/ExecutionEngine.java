package network.piranya.platform.node.core.execution.engine;

import static network.piranya.platform.node.utilities.CollectionUtils.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import network.piranya.platform.api.exceptions.PiranyaException;
import network.piranya.platform.api.exceptions.PricesNotAvailableException;
import network.piranya.platform.api.extension_models.ExtensionContext;
import network.piranya.platform.api.extension_models.ParametersBuilder;
import network.piranya.platform.api.extension_models.execution.bots.Bot;
import network.piranya.platform.api.extension_models.execution.bots.BotEvent;
import network.piranya.platform.api.extension_models.execution.bots.ExecutionBot;
import network.piranya.platform.api.extension_models.execution.liquidity.LiquidityProvider;
import network.piranya.platform.api.extension_models.execution.sandbox.SandboxConfiguration;
import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.api.lang.Result;
import network.piranya.platform.api.models.bots.BotView;
import network.piranya.platform.api.models.info.MarketInfoProvider;
import network.piranya.platform.api.models.log.ActivityLog;
import network.piranya.platform.api.models.search.SearchService;
import network.piranya.platform.api.models.trading.ExecutionEvent;
import network.piranya.platform.api.models.trading.Instrument;
import network.piranya.platform.api.models.trading.Quote;
import network.piranya.platform.api.models.trading.TradingState;
import network.piranya.platform.api.models.trading.liquidity.QuoteEvent;
import network.piranya.platform.node.api.booting.NetworkNodeConfig;
import network.piranya.platform.node.api.execution.ExecutionManager;
import network.piranya.platform.node.api.execution.bots.BotsRegistry;
import network.piranya.platform.node.api.execution.commands.AbortBot;
import network.piranya.platform.node.api.execution.commands.InvokeExecutionBotCommand;
import network.piranya.platform.node.api.execution.liquidity.LiquidityProvidersRegistry;
import network.piranya.platform.node.api.execution.commands.CreateBot;
import network.piranya.platform.node.api.execution.commands.ExecutionCommand;
import network.piranya.platform.node.api.execution.commands.InvokeCommandBot;
import network.piranya.platform.node.api.local_infrastructure.LocalServices;
import network.piranya.platform.node.api.local_infrastructure.concurrency.Executor;
import network.piranya.platform.node.core.execution.analytics.AnalyticsEngine;
import network.piranya.platform.node.core.execution.analytics.AnalyticsImpl;
import network.piranya.platform.node.core.execution.context.ManagerialExtensionContextImpl;
import network.piranya.platform.node.core.execution.engine.activity_log.EsLog;
import network.piranya.platform.node.core.execution.engine.activity_log.BotAbortedLog;
import network.piranya.platform.node.core.execution.engine.activity_log.BotEsLog;
import network.piranya.platform.node.core.execution.engine.activity_log.BotCommandedLog;
import network.piranya.platform.node.core.execution.engine.activity_log.FillLog;
import network.piranya.platform.node.core.execution.engine.activity_log.OrderCancelReplyLog;
import network.piranya.platform.node.core.execution.engine.activity_log.OrderPlacementReplyLog;
import network.piranya.platform.node.core.execution.engine.bots.BotsOperator;
import network.piranya.platform.node.core.execution.engine.infrastructure.storage.StorageOperator;
import network.piranya.platform.node.core.execution.engine.sandbox.SandboxEngine;
import network.piranya.platform.node.utilities.BotUtils;

public class ExecutionEngine {
	
	@SuppressWarnings("unchecked")
	public void execute(ExecutionCommand command) {
		if (command instanceof CreateBot) {
			CreateBot c = (CreateBot)command;
			try {
				String botTypeId = c.botTypeId().orElseGet(() -> botsRegistry().getBotTypeByFeature(c.featureId().get(), c.params()));
				
				Class<Bot> botType = botsRegistry().botType(botTypeId);
				if (!ExecutionBot.class.isAssignableFrom(botType)) {
					throw new PiranyaException(String.format("Bot Type '%s' is not an execution bot", botTypeId));
				}
				if (BotUtils.isSingleton((Class<ExecutionBot>)(Class<?>)botType)) {
					c.resultHandler().accept(new Result<>(new PiranyaException(String.format("Bot Type '%s' is singleton", botTypeId))));
				}
				
				BotView view = botsOperator().createBot(botTypeId, c.params(), Optional.empty(), c.eventsSubscriber());
				//trail().append(new BotCreatedLog(botTypeId, c.params(), view.ref().botId()));
				//command.eventsSubscriber().ifPresent(subscriber -> optionalRunningActor(activity.actorId()).ifPresent(ra -> ra.eventSubscribers().add(subscriber)));
				c.resultHandler().accept(new Result<>(view));
			} catch (Exception ex) {
				c.resultHandler().accept(new Result<>(ex));
			}
		} else if (command instanceof InvokeExecutionBotCommand) {
			InvokeExecutionBotCommand c = (InvokeExecutionBotCommand)command;
			botsOperator().commandBot(c.botRef(), c.commandId(), c.params(), c.resultHandler(),
					() -> trail().append(new BotCommandedLog(c.botRef().botId(), c.commandId(), c.params())));
		} else if (command instanceof AbortBot) {
			AbortBot c = (AbortBot)command;
			BotView agentView = botsOperator().abortBot(c.botRef());
			trail().append(new BotAbortedLog(c.botRef().botId()));
			c.resultHandler().accept(new Result<>(agentView));
		} else if (command instanceof InvokeCommandBot) {
			InvokeCommandBot c = (InvokeCommandBot)command;
			detachedThreadsExecutor().execute(() -> botsRegistry().commandBotInvoker(c.spec(), c.commandId()).invoke(c.params(), c.resultHandler(),
					() -> new ManagerialExtensionContextImpl(createDetachedExtensionContext(""), liquidityProvidersRegistry())));
		} else {
			throw new PiranyaException(String.format("Command of type '%s' not supported", command.getClass().getName()));
		}
	}
	
	public TradingState tradingState() {
		return new TradingStateImpl(tradingRepository());
	}
	
	public void subscribe(Consumer<ExecutionEvent> subscriber) {
		externalSubscribers().put(subscriber, true);
	}
	
	public void unsubscribe(Consumer<ExecutionEvent> subscriber) {
		externalSubscribers().remove(subscriber);
	}
	
	public ExtensionContext createDetachedExtensionContext(String moduleId) {
		return botsOperator().createDetachedExtensionContext(moduleId);
	}
	
	public Quote quote(Instrument instrument) throws PricesNotAvailableException {
		Instrument inst = instrument;
		Quote quote = quotesMap().get(inst.symbol());
		while (quote == null && inst.sources().length > 0) {
			inst = inst.zoomOutSource();
			quote = quotesMap().get(inst.symbol());
		}
		
		if (quote != null) {
			return quote;
		} else {
			throw new PricesNotAvailableException(instrument);
		}
	}
	
	
	protected void onBranchLog(EsLog log) {
		trail().append(log);
	}
	
	protected void onBotEvent(BotEvent event) {
		onExecutionEvent(event, true);
	}
	
	protected void onBranchEvent(ExecutionEvent event, boolean dontSave) {
		onExecutionEvent(event, dontSave);
	}
	
	protected void onExecutionEvent(ExecutionEvent event, boolean dontSave) {
		if (acceptExecutionEvent(event) && !(event instanceof BotEvent) && !dontSave) {
			trail().append(event);
		}
		
		if (event instanceof QuoteEvent) {
			handleQuoteEvent((QuoteEvent)event);
		}
		
		/// if not replaying, publish to external subscribers
		if (!isReplaying()) {
			externalEventsExecutor().execute(() -> foreachIgnoreExceptions(externalSubscribers().keySet(), s -> s.accept(event)));
		}
	}
	
	protected void handleQuoteEvent(QuoteEvent quoteEvent) {
		Quote quote = quoteEvent.quote();
		Instrument inst = quote.instrument();
		quotesMap().put(inst.symbol(), quote);
		while (inst.sources().length > 0) {
			inst = inst.zoomOutSource();
			quotesMap().put(inst.symbol(), quote);
		}
	}
	
	protected boolean acceptExecutionEvent(ExecutionEvent event) {
		if (event instanceof QuoteEvent) {
			if (isReplaying()) {
				QuoteEvent q = (QuoteEvent) event;
				lpExectuionBranch(q.instrument().sourceId()).acceptQuote(q.quote());
			}
		}
		
		/// if bot event, bots operator doesn't need to process it since it's the one who published it in the first place
		return event instanceof BotEvent ? false : botsOperator().acceptExecutionEvent(event);
	}
	
	
	/*
	protected void onReplicate() {
		// store order/fill marking entries
		// rest store optionally in a separate log/trail per node
	}
	*/
	// trading/orders repository? define no scope
	
	
	public void init() {
		liquidityProvidersRegistry().subscribe(this.lpEventsListener);
		foreach(liquidityProvidersRegistry().liquidityProviders(),
				lp -> lpExecutionBranches().put(lp.liquidityProviderId(), createLpExecutionBranch(lp)));
		
		replay();
	}
	
	@SuppressWarnings("unchecked")
	protected void replay() {
		setReplaying(true);
		
		storageOperator().startReplay();
		botsOperator().startReplay();
		
		foreach(lpExecutionBranches().values(), lpb -> lpb.startReplay());
		
		trail().replay(log -> {
			if (log instanceof BotEsLog) {
				botsOperator().accept((BotEsLog)log);
			} else if (log instanceof OrderPlacementReplyLog) {
				OrderPlacementReplyLog l = (OrderPlacementReplyLog)log;
				lpExectuionBranch(l.liquidityProviderId()).acceptActivityLogEntry(log);
			} else if (log instanceof FillLog) {
				FillLog l = (FillLog)log;
				lpExectuionBranch(l.liquidityProviderId()).acceptActivityLogEntry(log);
			} else if (log instanceof OrderCancelReplyLog) {
				OrderCancelReplyLog l = (OrderCancelReplyLog)log;
				lpExectuionBranch(l.liquidityProviderId()).acceptActivityLogEntry(log);
			} else {
				throw new PiranyaException(String.format("Activity Log of type '%s' not supported", log.getClass().getName()));
			}
		}, this::acceptExecutionEvent);
		
		botsOperator().finishReplay();
		storageOperator().finishReplay();
		
		foreach(lpExecutionBranches().values(), lpb -> lpb.finishReplay());
		
		setReplaying(false);
		
		foreach(filter(botsRegistry().botTypes(), botType -> BotUtils.isSingleton((Class<ExecutionBot>)(Class<?>)botType)
				&& !find(botsRegistry.botsList(), bot -> botType.isInstance(bot)).isPresent()),
				singletonBotType -> botsOperator().createBot(singletonBotType.getName(), new ParametersBuilder().build(), Optional.empty(), Optional.empty()));
	}
	
	public void dispose() {
		liquidityProvidersRegistry().unsubscribe(this.lpEventsListener);
		trail().dispose();
		if (this.activityLogger != null) this.activityLogger.dispose();
	}
	
	protected LpExectuionBranch lpExectuionBranch(String liquidityProviderId) {
		LpExectuionBranch branch = lpExecutionBranches().get(liquidityProviderId);
		if (branch == null) {
			if (isReplaying()) {
				branch = createLpExecutionBranch(new UnavailableLp(liquidityProviderId));
				branch.startReplay();
				lpExecutionBranches().put(liquidityProviderId, branch);
			} else {
				throw new PiranyaException(String.format("Liquidity Provider '%s' was not found", liquidityProviderId));
			}
		}
		return branch;
	}
	
	private final ConcurrentMap<String, LpExectuionBranch> lpExecutionBranches = new ConcurrentHashMap<>();
	protected ConcurrentMap<String, LpExectuionBranch> lpExecutionBranches() {
		return lpExecutionBranches;
	}
	
	private final Consumer<LiquidityProvidersRegistry.LpRegistrationEvent> lpEventsListener = event -> {
		if (event.eventType() == LiquidityProvidersRegistry.LpRegistrationEvent.EventType.REGISTERED) {
			LpExectuionBranch existingBranch = lpExecutionBranches().get(event.liquidityProvider().liquidityProviderId());
			if (existingBranch != null/* && existingBranch.liquidityProvider() instanceof UnavailableLp*/) {
				existingBranch.updateLiquidityProvider(event.liquidityProvider());
			} else {
				lpExecutionBranches().put(event.liquidityProvider().liquidityProviderId(), createLpExecutionBranch(event.liquidityProvider()));
			}
		} else {
			lpExecutionBranches().remove(event.liquidityProvider().liquidityProviderId());
		}
	};
	
	protected LpExectuionBranch createLpExecutionBranch(LiquidityProvider lp) {
		return new LpExectuionBranch(lp, tradingRepository(), ExecutionEngine.this::onBranchLog, ExecutionEngine.this::onBranchEvent, logActivity(), lpThreadsExecutor());
	}
	
	
	public ExecutionEngine(NetworkNodeConfig config, LocalServices localServices, BotsRegistry botsRegistry, LiquidityProvidersRegistry liquidityProvidersRegistry,
			SearchService searchService/*, Threading?*/, MarketInfoProvider marketInfoProvider, ExecutionManager executionManager) {
		this.config = config;
		this.localServices = localServices;
		this.botsRegistry = botsRegistry;
		this.liquidityProvidersRegistry = liquidityProvidersRegistry;
		this.searchService = searchService/*, Threading?*/;
		this.marketInfoProvider = marketInfoProvider;
		this.executionManager = executionManager;
		this.detachedThreadsExecutor = localServices().separateExecutor(new Executor.Config(3)); // TODO threads amount configurable
		this.lpThreadsExecutor = localServices().separateExecutor(new Executor.Config(1));
		this.externalEventsExecutor = localServices().separateExecutor(new Executor.Config(1));
		this.logActivity = createActivityLogger();
		this.tradingRepository = new TradingRepository(logActivity());
		
		this.trail = newExecutionTrail();
		this.storageOperator = new StorageOperator(localServices().localStorage());
		this.botsOperator = new BotsOperator(Integer.valueOf(config.nodeId().localIndex()).toString(), botsRegistry, storageOperator(), tradingRepository(),
				detachedThreadsExecutor(), this::lpExectuionBranch, log -> trail().append(log), this::onBotEvent, this::execute,
				() -> new AnalyticsImpl(this.analyticsEngine), this::newSandboxEngine, searchService, marketInfoProvider, this.activityLogger, executionManager());
	}
	
	protected Consumer<ActivityLog> createActivityLogger() {
		this.activityLogger = newActivityLogger();
		this.analyticsEngine = new AnalyticsEngine(activityLogger, localServices(), marketInfoProvider());
		
		return log -> {
			if (!isReplaying()) {
				activityLogger.log(log);
			}
		};
	}
	
	protected ActivityLogger newActivityLogger() {
		return new ActivityLogger(localServices().localStorage(), localServices().separateExecutor(new Executor.Config(1)));
	}
	
	protected ExecutionTrail newExecutionTrail() {
		return new ExecutionTrail(localServices().localStorage());
	}
	
	protected SandboxEngine newSandboxEngine(SandboxConfiguration configuration) {
		return new SandboxEngine(configuration, config(), localServices(), botsRegistry(), liquidityProvidersRegistry(), searchService(), marketInfoProvider());
	}
	
	private final NetworkNodeConfig config;
	protected NetworkNodeConfig config() {
		return config;
	}
	
	private final LocalServices localServices;
	protected LocalServices localServices() { return localServices; }
	
	private final BotsRegistry botsRegistry;
	protected BotsRegistry botsRegistry() { return botsRegistry; }
	
	private final LiquidityProvidersRegistry liquidityProvidersRegistry;
	protected LiquidityProvidersRegistry liquidityProvidersRegistry() { return liquidityProvidersRegistry; }
	
	private final ExecutionTrail trail;
	protected ExecutionTrail trail() { return trail; }
	
	private final BotsOperator botsOperator;
	protected BotsOperator botsOperator() { return botsOperator; }
	
	private final StorageOperator storageOperator;
	protected StorageOperator storageOperator() { return storageOperator; }
	
	private final TradingRepository tradingRepository;
	protected TradingRepository tradingRepository() { return tradingRepository; }
	
	private final ConcurrentMap<Consumer<ExecutionEvent>, Boolean> externalSubscribers = new ConcurrentHashMap<>();
	protected ConcurrentMap<Consumer<ExecutionEvent>, Boolean> externalSubscribers() { return externalSubscribers; }
	
	private final SearchService searchService;
	protected SearchService searchService() { return searchService; }
	
	private MarketInfoProvider marketInfoProvider;
	protected MarketInfoProvider marketInfoProvider() { return marketInfoProvider; }
	
	private ExecutionManager executionManager;
	protected ExecutionManager executionManager() { return executionManager; }
	
	private final Executor detachedThreadsExecutor;
	protected Executor detachedThreadsExecutor() { return detachedThreadsExecutor; }
	
	private final Executor lpThreadsExecutor;
	protected Executor lpThreadsExecutor() { return lpThreadsExecutor; }
	
	private final Executor externalEventsExecutor;
	protected Executor externalEventsExecutor() { return externalEventsExecutor; }
	
	private ActivityLogger activityLogger;
	
	private AnalyticsEngine analyticsEngine;
	public AnalyticsEngine analyticsEngine() { return analyticsEngine; }
	
	private final Consumer<ActivityLog> logActivity;
	protected Consumer<ActivityLog> logActivity() { return logActivity; }
	
	private boolean replaying = false;
	protected boolean isReplaying() { return replaying; }
	protected void setReplaying(boolean replaying) { this.replaying = replaying; }
	
	private final ConcurrentMap<String, Quote> quotesMap = new ConcurrentHashMap<>();
	protected ConcurrentMap<String, Quote> quotesMap() { return quotesMap; }
	
}
