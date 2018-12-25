package network.piranya.platform.api.extension_models.execution.liquidity;

import java.math.BigDecimal;
import network.piranya.platform.api.lang.Optional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import network.piranya.platform.api.exceptions.OperationNotSupportedException;
import network.piranya.platform.api.exceptions.PiranyaException;
import network.piranya.platform.api.extension_models.ManagerialExtensionContext;
import network.piranya.platform.api.extension_models.Parameters;
import network.piranya.platform.api.extension_models.execution.liquidity.LpConnectionStatus.Status;
import network.piranya.platform.api.lang.None;
import network.piranya.platform.api.lang.Result;
import network.piranya.platform.api.lang.ResultHandler;
import network.piranya.platform.api.models.metadata.LiquidityProviderInfo;
import network.piranya.platform.api.models.trading.ExchangeTrade;
import network.piranya.platform.api.models.trading.Instrument;
import network.piranya.platform.api.models.trading.OrderBook;
import network.piranya.platform.api.models.trading.Quote;
import network.piranya.platform.api.models.trading.filling.Fee;
import network.piranya.platform.api.models.trading.filling.Fill;
import network.piranya.platform.api.models.trading.liquidity.LpActivityInfo;
import network.piranya.platform.api.models.trading.liquidity.PriceSubscriptionOptions;
import network.piranya.platform.api.models.trading.ordering.PendingOrder;
import network.piranya.platform.api.utilities.Utilities;

//internally held (pipeline). consider proxying this class for internal use
public abstract class LiquidityProvider {
	
	public final String liquidityProviderId() {
		return info().getId();
	}
	
	public abstract void init();
	
	protected abstract String toLpSymbol(Instrument instrument);
	protected abstract Instrument toInstrument(String lpSymbol);
	
	protected abstract void doPlaceOrder(String symbol, PlaceOrderRequest request, Consumer<Result<PlaceOrderReply>> handler);
	protected abstract void doCancelOrder(String externalOrderId, Consumer<Result<None>> handler);
	
	public abstract void doSubscribeToPrices(Map<String, PriceSubscriptionOptions> symbolsOptions);
	public abstract void doUnsubscribeFromPrices(PriceSubscriptionOptions options, String... symbols);
	
	public abstract void doConnect(ResultHandler<None> handler);
	public abstract void doDisconnect(ResultHandler<None> handler);
	
	public abstract LpAccountState accountState();
	
	//ReplyHandler<HistoryItem> transactionsHistory();
	
	
	protected final Utilities utils() { return utils; }
	protected final Utilities utils = new Utilities();

	
	public final void connect(ResultHandler<None> handler) {
		updateState(state -> state.put("connect", true));
		doConnect(result -> {
			if (result.isSuccessful()) {
				updateActivityInfo(data -> data.put(LpActivityInfo.IS_CONNECTED, true));
			}
			handler.accept(result);
		});
	}
	
	public final void disconnect(ResultHandler<None> handler) {
		updateState(state -> state.put("connect", false));
		doDisconnect(result -> {
			if (result.isSuccessful()) {
				updateActivityInfo(data -> data.put(LpActivityInfo.IS_CONNECTED, false));
			}
			handler.accept(result);
		});
	}
	
	public void subscribeToPrices(Map<String, PriceSubscriptionOptions> symbolsOptions) {
		if (!symbolsOptions.isEmpty()) {
			doSubscribeToPrices(symbolsOptions);
			subscribedSymbols.addAll(symbolsOptions.keySet());
			
			updateActivityInfo(info -> info.put(LpActivityInfo.SUBSCRIBED_INSTRUMENTS_COUNT, subscribedSymbols.size()));
		}
	}
	
	public void subscribeToPrices(PriceSubscriptionOptions options, String... symbols) {
		Map<String, PriceSubscriptionOptions> symbolsOptions = new HashMap<>();
		for (String symbol : symbols) {
			symbolsOptions.put(symbol, options);
		}
		
		subscribeToPrices(symbolsOptions);
	}
	
	public void unsubscribeFromPrices(PriceSubscriptionOptions options, String... symbols) {
		doUnsubscribeFromPrices(options, symbols);
		
		for (String symbol : symbols) subscribedSymbols.remove(symbol);
		updateActivityInfo(info -> info.put(LpActivityInfo.SUBSCRIBED_INSTRUMENTS_COUNT, subscribedSymbols.size()));
	}
	
	public void measureLatency(ResultHandler<Long> handler) {
		throw new OperationNotSupportedException(String.format("Operation 'measureLatency' is not supported by Liquidity Provider '%s'", getClass().getSimpleName()));
	}
	
	protected final void updateLatency(long latency) {
		updateActivityInfo(info -> info.put(LpActivityInfo.LATENCY, latency));
	}
	
	private Set<String> subscribedSymbols = utils.col.set();
	
	protected final void updateState(Object customState) {
		this.customState = customState;
		updateState(state -> {});
	}
	private void updateState(Consumer<Map<String, Object>> updater) {
		Map<String, Object> newState = new HashMap<>(this.state);
		updater.accept(newState);
		this.state = Collections.unmodifiableMap(newState);
		
		this.stateUpdateConsumer.accept(customState, state);
	}
	
	protected final Map<String, Object> state() {
		return this.state;
	}
	
	protected final Object customState() {
		return this.customState;
	}
	
	public void acceptPlatformTradingState(List<PendingOrder> orders, List<Fill> fills) {
		
	}
	
	private Map<String, Object> state = new HashMap<>();
	private Object customState;
	private BiConsumer<Object, Map<String, Object>> stateUpdateConsumer;
	
	public final void placeOrder(PlaceOrderRequest request, Consumer<Result<PlaceOrderReply>> handler) {
		try {
			checkConnectionStatus();
			updateActivityInfo(info -> info.put(LpActivityInfo.LAST_ACTIVITY_AT, context().executor().now()));
			
			Instrument instrument = new Instrument(request.spec().symbol());
			if (liquidityProviderId().equals(instrument.sourceId())) {
				instrument = instrument.zoomOutSource();
				request = new PlaceOrderRequest(request.spec().updateSymbol(instrument.symbol()), request.refId(), request.externalTradeId(), request.tp(), request.sl());
			}
			
			doPlaceOrder(toLpSymbol(instrument), request, handler);
		} catch (Exception ex) {
			handler.accept(new Result<>(ex));
		}
	}
	
	public void cancelOrder(String externalOrderId, Consumer<Result<None>> handler) {
		try {
			checkConnectionStatus();
			updateActivityInfo(info -> info.put(LpActivityInfo.LAST_ACTIVITY_AT, context().executor().now()));
			
			doCancelOrder(externalOrderId, handler);
		} catch (Exception ex) {
			handler.accept(new Result<>(ex));
		}
	}
	
	public final LpActivityInfo activityInfo() {
		return activityInfo;
	}
	
	protected final void updateActivityInfo(Consumer<Map<String, Object>> updater) {
		Map<String, Object> newData = new HashMap<>(this.activityInfo.getData());
		updater.accept(newData);
		this.activityInfo = new LpActivityInfo(Collections.unmodifiableMap(newData));
	}
	
	private LpActivityInfo activityInfo = new LpActivityInfo(Collections.unmodifiableMap(new HashMap<>()));
	
	
	private Instrument legitimize(Instrument instrument) {
		return info().getId().equals(instrument.sourceId()) ? instrument : new Instrument(instrument.symbol(), info().getId());
	}
	
	
	public final LiquidityProviderInfo info() {
		return info;
	}
	/// INJECTED
	private LiquidityProviderInfo info;
	
	
	protected final void acceptFill(String externalFillId, String externalOrderId, String externalTradeId, String symbol, BigDecimal price, BigDecimal size) {
		acceptFill(new AcceptFill(externalFillId, externalOrderId, externalTradeId, legitimize(toInstrument(symbol)), price, size, new Fee[0], context().executor().now()));
	}
	
	protected final void acceptFill(AcceptFill fill) {
		updateActivityInfo(info -> info.put(LpActivityInfo.LAST_ACTIVITY_AT, context().executor().now()));
		publish(fill);
	}
	
	protected final void acceptQuote(String symbol, BigDecimal bid, BigDecimal ask) {
		Quote quote = new Quote(legitimize(toInstrument(symbol)), bid, ask);
		lastQuotes.put(quote.instrument().symbol(), quote);
		publish(new LpQuote(quote));
	}
	protected final void acceptQuote(String symbol, OrderBook orderBook) {
		Instrument instrument = legitimize(toInstrument(symbol));
		// later see a way to publish the quote with complete order book as an event, but save LpQuote event with only updates to order book
		Quote last = lastQuotes.get(instrument.symbol());
		if (last != null) {
			if (last.orderBook().isPresent()) {
				orderBook = last.orderBook().get().updateFrom(orderBook);
			}
		}
		
		Quote quote = new Quote(instrument, orderBook);
		lastQuotes.put(instrument.symbol(), quote);
		publish(new LpQuote(quote));
	}
	
	protected final void acceptExchangeTrade(ExchangeTrade exchangeTrade) {
		publish(new LpExchangeTrade(exchangeTrade));
	}
	
	protected final void onConnected() {
		publish(new LpConnectionEvent(liquidityProviderId(), LpConnectionEvent.EventType.CONNECTED));
	}
	
	protected final void onDisconnected() {
		publish(new LpConnectionEvent(liquidityProviderId(), LpConnectionEvent.EventType.DISCONNECTED));
	}
	
	public final Optional<Quote> getQuote(String symbol) {
		Quote quote = lastQuotes.get(symbol);
		return quote != null ? Optional.of(quote) : Optional.empty();
	}
	
	public final Optional<Quote> getQuote(Instrument instrument) {
		return getQuote(instrument.symbol());
	}
	
	protected final void registerInstrument(Instrument instrument) {
		instrumentRegistrationConsumer.accept(instrument);
		allInstruments.add(instrument);
		updateActivityInfo(info -> info.put(LpActivityInfo.TOTAL_INSTRUMENTS_COUNT, allInstruments.size()));
	}
	private Consumer<Instrument> instrumentRegistrationConsumer;
	private final Set<Instrument> allInstruments = utils.col.set();
	
	
	public final void subscribe(Consumer<LpEvent> subscriber) {
		subscribers.put(subscriber, true);
	}
	
	public final void unsubscribe(Consumer<LpEvent> subscriber) {
		subscribers.remove(subscriber);
	}
	
	protected final void publish(LpEvent event) {
		subscribers.keySet().stream().forEach(subscriber -> {
			try { subscriber.accept(event); }
			catch (Throwable ex) {}
		});
	}
	
	private final ConcurrentMap<Consumer<LpEvent>, Boolean> subscribers = new ConcurrentHashMap<>();
	
	private final ConcurrentMap<String, Quote> lastQuotes = new ConcurrentHashMap<>();
	
	protected final void checkConnectionStatus() {
		if (connectionStatus().status() != Status.ONLINE) {
			throw new PiranyaException(String.format("Connection to liquidity provider is '%s'", connectionStatus().status()));
		}
	}
	
	public final LpConnectionStatus connectionStatus() {
		return connectionStatus;
	}
	protected final void setConnectionStatus(LpConnectionStatus connectionStatus) {
		this.connectionStatus = connectionStatus;
	}
	private LpConnectionStatus connectionStatus = new LpConnectionStatus(Status.OFFLINE);
	
	
	protected final ManagerialExtensionContext context() { return context; }
	private ManagerialExtensionContext context;
	
	
	public final void dispose() {
		try { doDisconnect(result -> {}); }
		catch (Throwable ex) { }
	}
	
	
	public LiquidityProvider() {
		state.put("connect", false);
		this.state = Collections.unmodifiableMap(state);
		
		updateActivityInfo(data -> data.put(LpActivityInfo.IS_CONNECTED, false));
		updateActivityInfo(info -> {
			info.put(LpActivityInfo.SUBSCRIBED_INSTRUMENTS_COUNT, 0);
		});
	}
	
	private Parameters params;
	public Parameters params() { return params; }
	
}
