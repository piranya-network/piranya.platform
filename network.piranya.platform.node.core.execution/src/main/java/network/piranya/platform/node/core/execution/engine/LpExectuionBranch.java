package network.piranya.platform.node.core.execution.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import network.piranya.platform.api.extension_models.execution.bots.ExecutionBot;
import network.piranya.platform.api.extension_models.execution.bots.ExecutionBot.ExecutionContext;
import network.piranya.platform.api.extension_models.execution.liquidity.AcceptFill;
import network.piranya.platform.api.extension_models.execution.liquidity.LiquidityProvider;
import network.piranya.platform.api.extension_models.execution.liquidity.LpConnectionEvent;
import network.piranya.platform.api.extension_models.execution.liquidity.LpEvent;
import network.piranya.platform.api.extension_models.execution.liquidity.LpExchangeTrade;
import network.piranya.platform.api.extension_models.execution.liquidity.LpQuote;
import network.piranya.platform.api.extension_models.execution.liquidity.PlaceOrderRequest;
import network.piranya.platform.api.lang.None;
import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.api.lang.WithinContextReplyHandler;
import network.piranya.platform.api.models.bots.BotRef;
import network.piranya.platform.api.models.log.ActivityLog;
import network.piranya.platform.api.models.log.LpFillLog;
import network.piranya.platform.api.models.log.LpOrderAcceptedLog;
import network.piranya.platform.api.models.log.LpOrderPlaceLog;
import network.piranya.platform.api.models.log.LpOrderRejectedLog;
import network.piranya.platform.api.models.trading.ExecutionEvent;
import network.piranya.platform.api.models.trading.Instrument;
import network.piranya.platform.api.models.trading.Quote;
import network.piranya.platform.api.models.trading.filling.Fill;
import network.piranya.platform.api.models.trading.filling.FillEvent;
import network.piranya.platform.api.models.trading.liquidity.ExchangeTradeEvent;
import network.piranya.platform.api.models.trading.liquidity.LiquidityProviderRef;
import network.piranya.platform.api.models.trading.liquidity.PriceSubscriptionOptions;
import network.piranya.platform.api.models.trading.liquidity.QuoteEvent;
import network.piranya.platform.api.models.trading.ordering.PendingOrder;
import network.piranya.platform.api.models.trading.ordering.OrderCancelledEvent;
import network.piranya.platform.api.models.trading.ordering.OrderDescription;
import network.piranya.platform.api.models.trading.ordering.OrderPlacedEvent;
import network.piranya.platform.api.models.trading.ordering.OrderRef;
import network.piranya.platform.api.models.trading.ordering.OrderSpec;
import network.piranya.platform.api.models.trading.ordering.OrderingProgress;
import network.piranya.platform.node.api.local_infrastructure.concurrency.Executor;
import network.piranya.platform.node.core.execution.engine.activity_log.EsLog;
import network.piranya.platform.node.core.execution.engine.activity_log.FillLog;
import network.piranya.platform.node.core.execution.engine.activity_log.OrderCancelReplyLog;
import network.piranya.platform.node.core.execution.engine.activity_log.OrderPlacementReplyLog;
import network.piranya.platform.node.utilities.TimeService;
import network.piranya.platform.node.utilities.impl.WithinContextReplyHandlerImpl;

public class LpExectuionBranch extends ExternalExecutionBranch {
	
	public WithinContextReplyHandler<OrderRef, ExecutionBot.ExecutionContext> placeOrder(OrderRef.PlatformOrderRef orderRef, OrderSpec spec,
			BotRef creatorBotRef, Optional<OrderDescription> optionalDescription, Supplier<ExecutionContext> executionContextSupplier) {
		PlaceOrderRequest request = new PlaceOrderRequest(spec, Optional.of(orderRef.orderId()), Optional.empty(), Optional.empty(), Optional.empty());
		OrderDescription description = optionalDescription.orElse(new OrderDescription());
		WithinContextReplyHandlerImpl<OrderRef, ExecutionBot.ExecutionContext> replyHandler = new WithinContextReplyHandlerImpl<>();
		logActivity().accept(new LpOrderPlaceLog(TimeService.now(), orderRef, creatorBotRef, spec, liquidityProviderId(),
				optionalDescription, liquidityProvider().getQuote(spec.symbol())));
		System.err.println("place order: " + orderRef);
		if (isReplaying()) {
			replayingOrderRequests.put(orderRef, new ReplayingOrderRequest(orderRef, request, creatorBotRef, description, replyHandler, executionContextSupplier));
		} else {
			lpThreadsExecutor().execute(() -> {
				//System.err.println("a");
				liquidityProvider().placeOrder(request, reply -> {
					try {
					//System.err.println("b");
					branchLogConsumer().accept(new OrderPlacementReplyLog(orderRef, creatorBotRef, liquidityProvider().liquidityProviderId(), reply.isSuccessful(),
							reply.result().map(r -> r.externalOrderId()), reply.error().map(ex -> ex.getMessage()), TimeService.now()));
					
					// TODO handle error message. if null, value is not present
					acceptOrderingReply(orderRef, creatorBotRef, request, description, reply.isSuccessful(), reply.result().map(r -> r.externalOrderId()),
							reply.error().map(error -> error.getMessage()), TimeService.now(), replyHandler, executionContextSupplier);
					//System.err.println("c");
					} catch (Throwable ex) {
						ex.printStackTrace();
					}
				});
			});
		}
		return replyHandler;
	}
	
	public WithinContextReplyHandler<None, ExecutionBot.ExecutionContext> cancelOrder(OrderRef orderRef, Supplier<ExecutionContext> executionContextSupplier) {
		WithinContextReplyHandlerImpl<None, ExecutionBot.ExecutionContext> replyHandler = new WithinContextReplyHandlerImpl<>();
		String externalOrderId = orderRef instanceof OrderRef.PlatformOrderRef
				? tradingRepository().get(orderRef).externalOrderRef().externalOrderId()
				: ((OrderRef.DetachedOrderRef)orderRef).externalOrderId();
				
		if (isReplaying()) {
			replayingCancelRequests.put(orderRef, new ReplayingCancelRequest(orderRef, replyHandler, executionContextSupplier));
		} else {
			lpThreadsExecutor().execute(() -> {
				liquidityProvider().cancelOrder(externalOrderId, reply -> {
					branchLogConsumer().accept(new OrderCancelReplyLog(orderRef, liquidityProviderId(),
							reply.isSuccessful(), reply.error().map(error -> error.getMessage()), TimeService.now()));
					
					acceptCancellingReply(orderRef, replyHandler, executionContextSupplier, reply.isSuccessful(), reply.error().map(error -> error.getMessage()), TimeService.now());
				});
			});
		}
		return replyHandler;
	}
	
	public Optional<Quote> quote(Instrument instrument) {
		Instrument i = liquidityProviderId().equals(instrument.sourceId()) ? instrument : instrument.appendSource(liquidityProviderId());
		
		Quote q = quotesMap().get(i.symbol());
		return q != null ? Optional.of(q) : Optional.empty();
	}
	
	public void acceptQuote(Quote quote) {
		quotesMap().put(quote.instrument().symbol(), quote);
	}
	
	public void subscribeToPrices(PriceSubscriptionOptions options, String... symbols) {
		liquidityProvider().subscribeToPrices(options, symbols);
		
		for (String symbol : symbols) {
			SymbolSubscription subscription = subscribedSymbols.get(symbol);
			if (subscription == null) {
				subscribedSymbols.put(symbol, new SymbolSubscription(symbol, options));
			} else {
				subscription.updateOptions(options);
			}
		}
	}
	
	public void unsubscribeFromPrices(PriceSubscriptionOptions options, String... symbols) {
		liquidityProvider().unsubscribeFromPrices(options, symbols);
		
		for (String symbol : symbols) {
			subscribedSymbols.remove(symbol);
		}
	}
	
	private Map<String, SymbolSubscription> subscribedSymbols = new HashMap<>();
	
	protected class SymbolSubscription {
		
		private final String symbol;
		public String symbol() { return symbol; }
		
		private PriceSubscriptionOptions options;
		public PriceSubscriptionOptions options() { return options; }
		
		public void updateOptions(PriceSubscriptionOptions update) {
			this.options = new PriceSubscriptionOptions(this.options.isSubscribeToOrderBook() || update.isSubscribeToOrderBook(),
					this.options.isSubscribeToExchangeTrades() || update.isSubscribeToExchangeTrades());
		}
		
		public SymbolSubscription(String symbol, PriceSubscriptionOptions options) {
			this.symbol = symbol;
			this.options = options;
		}
	}
	
	
	protected void acceptOrderingReply(OrderRef.PlatformOrderRef orderRef, BotRef creatorBotRef, PlaceOrderRequest request, OrderDescription description, boolean isSuccessful,
			Optional<String> externalOrderId, Optional<String> errorMessage, long time,
			WithinContextReplyHandlerImpl<OrderRef, ExecutionBot.ExecutionContext> replyHandler, Supplier<ExecutionContext> executionContextSupplier) {
		System.err.println("accept order: " + orderRef + ": " + externalOrderId + ": " + isSuccessful + ": " + errorMessage);
		if (isSuccessful) {
			PendingOrder pendingOrder = new PendingOrder(orderRef, creatorBotRef, request.spec(),
					new OrderRef.ExternalOrderRef(externalOrderId.get(), new LiquidityProviderRef(liquidityProviderId())), description, time, new OrderingProgress());
			tradingRepository().addOrder(pendingOrder);
			execute(executionContextSupplier, context -> replyHandler.doReply(orderRef, context));
			branchEventsConsumer().accept(new OrderPlacedEvent(pendingOrder), true);
			logActivity().accept(new LpOrderAcceptedLog(TimeService.now(), pendingOrder));
		} else {
			execute(executionContextSupplier, context -> replyHandler.doError(new RuntimeException(errorMessage.get()), context));
			logActivity().accept(new LpOrderRejectedLog(TimeService.now(), orderRef, errorMessage.get(), liquidityProviderId(), creatorBotRef));
		}
	}
	
	protected void acceptCancellingReply(OrderRef orderRef, WithinContextReplyHandlerImpl<None, ExecutionBot.ExecutionContext> replyHandler,
			Supplier<ExecutionBot.ExecutionContext> executionContextSupplier, boolean isSuccessful, Optional<String> errorMessage, long time) {
		if (isSuccessful) {
			PendingOrder removedOrder = tradingRepository().removeOrder(orderRef);
			execute(executionContextSupplier, context -> replyHandler.doReply(None.VALUE, context));
			branchEventsConsumer().accept(new OrderCancelledEvent(removedOrder), true);
		} else {
			execute(executionContextSupplier, context -> replyHandler.doError(new RuntimeException(errorMessage.get()), context));
		}
	}
	
	protected void acceptFill(FillLog log) {
		System.err.println("**** accept fill: " + log.symbol() + ": " + log.externalOrderId() + " @ " + log.price());
		System.err.println(log.externalOrderId());
		Optional<PendingOrder> order = tradingRepository().findOrderByExternalId(log.externalOrderId(), liquidityProviderId());
		if (order.isPresent()) {
			Fill fill = tradingRepository().acceptFill(order.get(), log);
			System.err.println(tradingRepository().trades().size());
			branchEventsConsumer().accept(new FillEvent(fill.fillId(), fill.tradeId(), new Instrument(log.symbol(), log.liquidityProviderId()), log.size(), log.price(),
					log.time(), order.get(), log.externalFillId(), log.externalTradeId(), log.externalOrderId()), true);
			
			logActivity().accept(new LpFillLog(TimeService.now(), fill));
		} else {
			// add detached fill
		}
	}
	
	protected void acceptLpFill(AcceptFill fill) {
		FillLog log = new FillLog(fill.externalFillId(), fill.externalOrderId(), fill.externalTradeId(),
				liquidityProviderId(), fill.instrument().symbol(), fill.price(), fill.size(), fill.fees(), fill.time());
		branchLogConsumer().accept(log);
		acceptFill(log);
	}
	
	protected void acceptLpQuote(LpQuote quote) {
		Quote q = quote.quote();
		if (!liquidityProviderId().equals(q.instrument().sourceId())) {
			q = new Quote(quote.quote().instrument().appendSource(liquidityProviderId()), quote.quote());
		}
		
		acceptQuote(q);
		branchEventsConsumer().accept(new QuoteEvent(q), false);
	}
	
	
	protected void execute(Supplier<ExecutionBot.ExecutionContext> executionContextSupplier, Consumer<ExecutionBot.ExecutionContext> task) {
		ExecutionBot.ExecutionContext context = executionContextSupplier.get();
		try {
			task.accept(context);
		} finally {
			if (context instanceof AutoCloseable) {
				try { ((AutoCloseable)context).close(); }
				catch (Throwable ex) { }
			}
		}
	}
	
	
	@Override
	public void acceptActivityLogEntry(EsLog log) {
		if (log instanceof OrderPlacementReplyLog) {
			OrderPlacementReplyLog l = (OrderPlacementReplyLog)log;
			ReplayingOrderRequest r = replayingOrderRequests.get(l.orderRef());
			if (r != null) {
				acceptOrderingReply(l.orderRef(), l.creatorBotRef(), r.request, r.description,
						l.isSuccessful(), l.externalOrderId(), l.errorMessage(), l.time(), r.replyHandler, r.executionContextSupplier);
				replayingCancelRequests.remove(l.orderRef());
			}
		} else if (log instanceof OrderCancelReplyLog) {
			OrderCancelReplyLog l = (OrderCancelReplyLog)log;
			ReplayingCancelRequest r = replayingCancelRequests.get(l.orderRef());
			if (r != null) {
				acceptCancellingReply(l.orderRef(), r.replyHandler, r.executionContextSupplier, l.isSuccessful(), l.errorMessage(), l.time());
				replayingCancelRequests.remove(l.orderRef());
			}
		} else if (log instanceof FillLog) {
			acceptFill((FillLog)log);
		} else {
			throw new RuntimeException(String.format("Activity Log of type '%s' is not supported", log.getClass().getName()));
		}
	}
	
	@Override
	public /*Future*/void finishReplay() {
		// TODO
		//tradingRepository().incorporateLp();
		
		// see if any requests are left
			// for place order: 
			// for cancel order: 
		
		catchup();
		
		// what if LP is disconnected? Freeze all activity, grey out orders/trades. Speculative/DC state of orders/fills/trades in Trading State
			// on reconnect compare Branch state with LP state
		
		super.finishReplay();
	}
	// consider removing
	private final Consumer<LpEvent> subscriber = event -> {
		if (event instanceof AcceptFill) {
			acceptLpFill((AcceptFill)event);
		} else if (event instanceof LpQuote) {
			acceptLpQuote((LpQuote)event);
		} else if (event instanceof LpExchangeTrade) {
			branchEventsConsumer().accept(new ExchangeTradeEvent(((LpExchangeTrade)event).trade()), false);
		} else if (event instanceof LpConnectionEvent) {
			branchEventsConsumer().accept(event, true);
		}
	};
	
	protected void catchup() {
		if (liquidityProvider() instanceof UnavailableLp) {
			return ;
		}
		
		liquidityProvider().acceptPlatformTradingState(tradingRepository().orders(liquidityProviderId()), tradingRepository().fills(liquidityProviderId()));
		
		// if internal order exists but match (by externalId) doesn't, then cancel/
		// if internal fill exists but match doesn't, then
	}
	
	
	public LpExectuionBranch(LiquidityProvider liquidityProvider, TradingRepository tradingRepository,
			Consumer<EsLog> branchLogConsumer, BiConsumer<ExecutionEvent, Boolean> branchEventsConsumer, Consumer<ActivityLog> logActivity, Executor lpThreadsExecutor) {
		super(LpExectuionBranch.class.getSimpleName() + ":" + liquidityProvider.liquidityProviderId());
		this.liquidityProvider = liquidityProvider;
		this.tradingRepository = tradingRepository;
		this.branchLogConsumer = branchLogConsumer;
		this.branchEventsConsumer = branchEventsConsumer;
		this.logActivity = logActivity;
		this.lpThreadsExecutor = lpThreadsExecutor;
		
		liquidityProvider().subscribe(subscriber);
		//inject(liquidityProvider(), LiquidityProvider.class, "fillConsumer", (Consumer<AcceptFill>)this::acceptLpFill);
		//inject(liquidityProvider(), LiquidityProvider.class, "quoteConsumer", (Consumer<LpQuote>)this::acceptLpQuote);
	}
	
	private LiquidityProvider liquidityProvider;
	protected LiquidityProvider liquidityProvider() { return liquidityProvider; }
	
	public void updateLiquidityProvider(LiquidityProvider liquidityProvider) {
		this.liquidityProvider = liquidityProvider;
		
		catchup();
		
		Map<String, PriceSubscriptionOptions> symbolsOptions = new HashMap<>();
		for (SymbolSubscription s : subscribedSymbols.values()) {
			symbolsOptions.put(s.symbol(), s.options());
		}
		liquidityProvider.subscribeToPrices(symbolsOptions);
		
		liquidityProvider.subscribe(subscriber);
	}
	
	public String liquidityProviderId() { return liquidityProvider().liquidityProviderId(); }
	
	private final TradingRepository tradingRepository;
	protected TradingRepository tradingRepository() { return tradingRepository; }
	
	private final Consumer<EsLog> branchLogConsumer;
	protected Consumer<EsLog> branchLogConsumer() { return branchLogConsumer; }
	
	private final BiConsumer<ExecutionEvent, Boolean> branchEventsConsumer;
	protected BiConsumer<ExecutionEvent, Boolean> branchEventsConsumer() { return branchEventsConsumer; }
	
	private final Consumer<ActivityLog> logActivity;
	protected Consumer<ActivityLog> logActivity() { return logActivity; }
	
	private final Executor lpThreadsExecutor;
	protected Executor lpThreadsExecutor() { return lpThreadsExecutor; }
	
	private final ConcurrentMap<String, Quote> quotesMap = new ConcurrentHashMap<>();
	protected ConcurrentMap<String, Quote> quotesMap() { return quotesMap; }
	
	private Map<OrderRef.PlatformOrderRef, ReplayingOrderRequest> replayingOrderRequests = new HashMap<>();
	private Map<OrderRef, ReplayingCancelRequest> replayingCancelRequests = new HashMap<>();
	
	
	protected class ReplayingOrderRequest {
		
		public final OrderRef.PlatformOrderRef orderRef;
		public final PlaceOrderRequest request;
		public final BotRef creatorBotRef;
		public final OrderDescription description;
		public final WithinContextReplyHandlerImpl<OrderRef, ExecutionBot.ExecutionContext> replyHandler;
		public final Supplier<ExecutionContext> executionContextSupplier;
		
		public ReplayingOrderRequest(OrderRef.PlatformOrderRef orderRef, PlaceOrderRequest request, BotRef creatorBotRef, OrderDescription description,
				WithinContextReplyHandlerImpl<OrderRef, ExecutionBot.ExecutionContext> replyHandler, Supplier<ExecutionContext> executionContextSupplier) {
			this.orderRef = orderRef;
			this.request = request;
			this.creatorBotRef = creatorBotRef;
			this.description = description;
			this.replyHandler = replyHandler;
			this.executionContextSupplier = executionContextSupplier;
		}
	}
	
	protected class ReplayingCancelRequest {
		
		public final OrderRef orderRef;
		public final WithinContextReplyHandlerImpl<None, ExecutionBot.ExecutionContext> replyHandler;
		public final Supplier<ExecutionContext> executionContextSupplier;
		
		public ReplayingCancelRequest(OrderRef orderRef, WithinContextReplyHandlerImpl<None,
				ExecutionBot.ExecutionContext> replyHandler, Supplier<ExecutionBot.ExecutionContext> executionContextSupplier) {
			this.orderRef = orderRef;
			this.replyHandler = replyHandler;
			this.executionContextSupplier = executionContextSupplier;
		}
	}
	
}
