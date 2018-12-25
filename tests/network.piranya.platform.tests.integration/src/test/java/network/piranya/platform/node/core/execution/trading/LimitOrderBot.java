package network.piranya.platform.node.core.execution.trading;

import network.piranya.platform.api.extension_models.execution.bots.ExecutionBot;
import network.piranya.platform.api.extension_models.execution.bots.Command;
import network.piranya.platform.api.extension_models.execution.bots.EventProcessor;
import network.piranya.platform.api.models.trading.filling.FillEvent;
import network.piranya.platform.api.models.trading.liquidity.LiquidityProvider;
import network.piranya.platform.api.models.trading.ordering.OrderRef;
import network.piranya.platform.api.models.trading.ordering.OrderSpec;
import network.piranya.platform.api.models.trading.ordering.OrderType;

public class LimitOrderBot extends ExecutionBot {
	
	@Override
	public void onStart(ExecutionContext context) {
		System.err.println("LimitOrderBot.onStart");
		lp(context).placeOrder(orderSpec())
			.onReply((orderRef, c) -> this.orderRef = orderRef)
			.onError((error, c) -> { error.printStackTrace(); c.finishWithError(error);});
	}
	
	@EventProcessor
	public void onFill(FillEvent fill, ExecutionContext context) {
		// tag(fill) if needed? or tag in order placement
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
