package network.piranya.platform.node.core.execution.trading;

import network.piranya.platform.api.extension_models.execution.bots.ExecutionBot;
import network.piranya.platform.api.extension_models.execution.bots.EventProcessor;
import network.piranya.platform.api.models.trading.filling.FillEvent;
import network.piranya.platform.api.models.trading.ordering.OrderSpec;

public class MarketOrderBot extends ExecutionBot {
	
	@Override
	public void onStart(ExecutionContext context) {
		context.liquidityProviders().get(params().string("lp")).placeOrder(orderSpec()).onError((error, c) -> { error.printStackTrace(); c.finishWithError(error); });
	}
	
	@EventProcessor
	public void onEvent(FillEvent fill, ExecutionContext context) {
		// tag(fill) if needed? or tag in order placement
		context.finish();
	}
	
	// how a trade can go to a portfolio? Or is a portfolio a separate entity that has a state and provides a service?
	// what about trades/orders view?
	
	// fill auto directed from LP
	// catchup logic in engine itself
	
	
	protected OrderSpec orderSpec() {
		return new OrderSpec(params().string("symbol"), params().bool("is_buy"), params().decimal("size"));
	}
	
}
