package network.piranya.platform.node.core.execution.testing.support;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import network.piranya.platform.api.extension_models.execution.liquidity.LiquidityProvider;
import network.piranya.platform.api.extension_models.execution.liquidity.LiquidityProviderCategory;
import network.piranya.platform.api.extension_models.execution.liquidity.LiquidityProviderMetadata;
import network.piranya.platform.api.extension_models.execution.liquidity.LpAccountState;
import network.piranya.platform.api.extension_models.execution.liquidity.LpConnectionStatus;
import network.piranya.platform.api.extension_models.execution.liquidity.LpPendingOrder;
import network.piranya.platform.api.extension_models.execution.liquidity.LpTrade;
import network.piranya.platform.api.extension_models.execution.liquidity.PlaceOrderReply;
import network.piranya.platform.api.extension_models.execution.liquidity.PlaceOrderRequest;
import network.piranya.platform.api.lang.None;
import network.piranya.platform.api.lang.Result;
import network.piranya.platform.api.lang.ResultHandler;
import network.piranya.platform.api.models.trading.Instrument;
import network.piranya.platform.api.models.trading.liquidity.PriceSubscriptionOptions;
import network.piranya.platform.api.models.trading.ordering.OrderType;
import network.piranya.platform.node.utilities.CollectionUtils;

@LiquidityProviderMetadata(displayName = "Mock LP", category = LiquidityProviderCategory.TESTING)
public class LiquidityProviderMock extends LiquidityProvider {
	
	public void marketOrderHandler(BiConsumer<PlaceOrderRequest, Consumer<Result<PlaceOrderReply>>> handler) {
		this.marketOrderHandler = handler;
	}
	BiConsumer<PlaceOrderRequest, Consumer<Result<PlaceOrderReply>>> marketOrderHandler;
	
	
	@Override
	public void doPlaceOrder(String symbol, PlaceOrderRequest request, Consumer<Result<PlaceOrderReply>> handler) {
		if (request.spec().orderType() == OrderType.MARKET) {
			if (marketOrderHandler != null) {
				marketOrderHandler.accept(request, handler);
			} else {
				marketRequests.put(request.refId().get(), new PendingRequest(request, handler));
			}
		} else {
			if (isAutoAcceptPendingOrders) {
				handler.accept(new Result<>(new PlaceOrderReply("XO1")));
			} else {
				pendingRequests.put(request.refId().get(), new PendingRequest(request, handler));
			}
		}
	}
	
	private boolean isAutoAcceptPendingOrders = false;
	public void autoAcceptPendingOrders(boolean isAutoAcceptPendingOrders) {
		this.isAutoAcceptPendingOrders = isAutoAcceptPendingOrders;
	}
	
	@Override
	protected void doCancelOrder(String externalOrderId, Consumer<Result<None>> handler) {
		cancelRequests.put(externalOrderId, new CancelRequest(externalOrderId, handler));
	}
	
	@Override
	public void init() {
	}
	
	@Override
	public void doConnect(ResultHandler<None> handler) {
		setConnectionStatus(new LpConnectionStatus(LpConnectionStatus.Status.ONLINE));
	}
	
	@Override
	public void doDisconnect(ResultHandler<None> handler) {
		setConnectionStatus(new LpConnectionStatus(LpConnectionStatus.Status.OFFLINE));
	}
	
	@Override
	public LpAccountState accountState() {
		return new LpAccountState(new ArrayList<>(trades.values()), new ArrayList<>(pendingOrders.values()));
	}
	
	@Override
	public void doSubscribeToPrices(Map<String, PriceSubscriptionOptions> symbolsOptions) {
	}
	
	@Override
	public void doUnsubscribeFromPrices(PriceSubscriptionOptions options, String... symbols) {
	}
	
	
	public void acceptPendingOrders() {
		CollectionUtils.foreach(pendingRequests.values(), p -> p.handler.accept(new Result<>(new PlaceOrderReply("XO1"))));
	}
	
	public void acceptMarketOrders() {
		CollectionUtils.foreach(marketRequests.values(), p -> p.handler.accept(new Result<>(new PlaceOrderReply("XO1"))));
	}
	
	public void acceptCancelOrders() {
		CollectionUtils.foreach(cancelRequests.values(), p -> p.handler.accept(new Result<>(None.VALUE)));
	}
	
	public List<PlaceOrderRequest> pendingOrdersList() {
		return CollectionUtils.map(pendingRequests.values(), p -> p.request);
	}
	
	public List<PlaceOrderRequest> marketOrdersList() {
		return CollectionUtils.map(marketRequests.values(), p -> p.request);
	}
	
	public void fillPendingOrders() {
		CollectionUtils.foreach(pendingRequests.values(), p -> {
			acceptFill("XF" + fillId.incrementAndGet(), "XO1", "XT" + tradeId.incrementAndGet(), p.request.spec().symbol(), p.request.spec().price(), p.request.spec().size());
		});
		pendingRequests.clear();
	}
	private AtomicInteger fillId = new AtomicInteger(0);
	private AtomicInteger tradeId = new AtomicInteger(0);
	
	public void publishQuote(String symbol, BigDecimal bid, BigDecimal ask) {
		acceptQuote(symbol, bid, ask);
	}


	public LiquidityProviderMock() {
	}
	
	private final Map<String, LpTrade> trades = new HashMap<>();
	protected final Map<String, LpPendingOrder> pendingOrders = new HashMap<>();
	protected final Map<String, PendingRequest> pendingRequests = new HashMap<>();
	protected final Map<String, PendingRequest> marketRequests = new HashMap<>();
	protected final Map<String, CancelRequest> cancelRequests = new HashMap<>();
	
	
	protected class PendingRequest {
		
		public final PlaceOrderRequest request;
		public final Consumer<Result<PlaceOrderReply>> handler;
		
		public PendingRequest(PlaceOrderRequest request, Consumer<Result<PlaceOrderReply>> handler) {
			this.request = request;
			this.handler = handler;
		}
	}
	
	protected class CancelRequest {
		
		public final String externalOrderId;
		public final Consumer<Result<None>> handler;
		
		public CancelRequest(String externalOrderId, Consumer<Result<None>> handler) {
			this.externalOrderId = externalOrderId;
			this.handler = handler;
		}
	}
	
	@Override
	protected String toLpSymbol(Instrument instrument) {
		return instrument.symbol();
	}
	
	@Override
	protected Instrument toInstrument(String lpSymbol) {
		return new Instrument(lpSymbol);
	}
	
}
