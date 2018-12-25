package network.piranya.platform.node.core.execution.sandbox;

import java.util.function.Consumer;

import network.piranya.platform.api.extension_models.execution.liquidity.LiquidityProviderCategory;
import network.piranya.platform.api.extension_models.execution.liquidity.LiquidityProviderMetadata;
import network.piranya.platform.api.extension_models.execution.liquidity.PlaceOrderReply;
import network.piranya.platform.api.extension_models.execution.liquidity.PlaceOrderRequest;
import network.piranya.platform.api.lang.Result;
import network.piranya.platform.api.models.trading.ordering.OrderType;
import network.piranya.platform.node.core.execution.testing.support.LiquidityProviderMock;

@LiquidityProviderMetadata(displayName = "Mock LP", category = LiquidityProviderCategory.TESTING)
public class SandboxLiquidityProvider extends LiquidityProviderMock {
	
	@Override
	public void doPlaceOrder(String symbol, PlaceOrderRequest request, Consumer<Result<PlaceOrderReply>> handler) {
		if (request.spec().orderType() == OrderType.MARKET) {
			pendingRequests.put(request.refId().get(), new PendingRequest(request, handler));
			handler.accept(new Result<>(new PlaceOrderReply("XO1")));
			fillPendingOrders();
		} else {
			super.doPlaceOrder(symbol, request, handler);
		}
	}
	
}
