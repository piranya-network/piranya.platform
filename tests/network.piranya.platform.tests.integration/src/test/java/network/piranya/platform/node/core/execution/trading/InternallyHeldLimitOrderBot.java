package network.piranya.platform.node.core.execution.trading;

import network.piranya.platform.api.extension_models.DataBuilder;
import network.piranya.platform.api.extension_models.execution.bots.ExecutionBot;
import network.piranya.platform.api.extension_models.execution.bots.Command;
import network.piranya.platform.api.extension_models.execution.bots.EventProcessor;
import network.piranya.platform.api.models.trading.filling.FillEvent;
import network.piranya.platform.api.models.trading.liquidity.LiquidityProvider;
import network.piranya.platform.api.models.trading.liquidity.QuoteEvent;
import network.piranya.platform.api.models.trading.ordering.OrderRef;
import network.piranya.platform.api.models.trading.ordering.OrderSpec;
import network.piranya.platform.api.models.trading.ordering.OrderType;

public class InternallyHeldLimitOrderBot extends ExecutionBot {
	
	@Override
	public void onStart(ExecutionContext context) {
		lp(context).subscribeToPrices(params().string("symbol"));
	}
	
	@EventProcessor
	public void onQuote(QuoteEvent quote, ExecutionContext context) {
		OrderSpec spec = orderSpec();
		if (utils().ordering().matches(spec.price(), spec.orderType(), spec.isBuy(), quote.quote().bid(), quote.quote().ask())) {
			publishEvent("TRIGGERED", new DataBuilder().build());
			lp(context).placeOrder(orderSpec().updateOrderType(OrderType.MARKET)).onReply((orderRef, c) -> {
				this.orderRef = orderRef;
				publishEvent("PLACED", new DataBuilder().build());
			}).onError((error, c) -> c.finishWithError(error));
		}
	}
	
	@EventProcessor
	public void onFill(FillEvent fill, ExecutionContext context) {
		context.finish();
	}
	
	@Command(id = "CANCEL")
	public void cancel(ExecutionContext context) {
		lp(context).cancelOrder(orderRef).onReply((reply, c) -> c.finish()).onError((error, c) -> c.finishWithError(error));
	}
	
	
	protected LiquidityProvider<ExecutionContext> lp(ExecutionContext context) {
		return context.liquidityProviders().get(params().string("lp"));
	}
	
	
	protected OrderSpec orderSpec() {
		return new OrderSpec(params().string("symbol"), OrderType.LIMIT, params().bool("is_buy"), params().decimal("price"), params().decimal("size"));
	}
	
	private OrderRef orderRef;
	
}
