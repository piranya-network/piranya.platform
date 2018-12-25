package network.piranya.platform.node.core.execution.liquidity;

import java.util.function.Consumer;

import network.piranya.platform.api.extension_models.execution.liquidity.LpAccountState;
import network.piranya.platform.api.extension_models.execution.liquidity.PlaceOrderReply;
import network.piranya.platform.api.extension_models.execution.liquidity.PlaceOrderRequest;
import network.piranya.platform.api.lang.Result;
import network.piranya.platform.api.models.metadata.LiquidityProviderInfo;

// pipeline and balance management
public interface LiquidityProviderInvoker {	
	
	void placeOrder(PlaceOrderRequest request, Consumer<Result<PlaceOrderReply>> handler);
	
	void cancelOrder(String externalOrderId, Consumer<Result<Void>> handler);
	
	LiquidityProviderInfo info();
	
	void connect(Consumer<Result<Void>> handler);
	void disconnect(Consumer<Result<Void>> handler);
	
	LpAccountState state();
	
}
