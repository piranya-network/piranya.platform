package network.piranya.platform.node.core.execution.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import network.piranya.platform.api.exceptions.LpNotExistsException;
import network.piranya.platform.api.extension_models.execution.liquidity.LiquidityProvider;
import network.piranya.platform.api.extension_models.execution.liquidity.LpAccountState;
import network.piranya.platform.api.extension_models.execution.liquidity.PlaceOrderReply;
import network.piranya.platform.api.extension_models.execution.liquidity.PlaceOrderRequest;
import network.piranya.platform.api.lang.None;
import network.piranya.platform.api.lang.Result;
import network.piranya.platform.api.lang.ResultHandler;
import network.piranya.platform.api.models.metadata.LiquidityProviderInfo;
import network.piranya.platform.api.models.trading.Instrument;
import network.piranya.platform.api.models.trading.liquidity.PriceSubscriptionOptions;
import network.piranya.platform.node.utilities.ReflectionUtils;

public class UnavailableLp extends LiquidityProvider {
	
	@Override
	protected void doPlaceOrder(String symbol, PlaceOrderRequest request, Consumer<Result<PlaceOrderReply>> handler) {
		throw new LpNotExistsException(liquidityProviderId());
	}
	
	@Override
	protected void doCancelOrder(String externalOrderId, Consumer<Result<None>> handler) {
		throw new LpNotExistsException(liquidityProviderId());
	}
	
	@Override
	public void init() {
	}
	
	@Override
	protected String toLpSymbol(Instrument instrument) {
		return null;
	}
	
	@Override
	protected Instrument toInstrument(String lpSymbol) {
		return null;
	}
	
	@Override
	public void doSubscribeToPrices(Map<String, PriceSubscriptionOptions> symbolsOptions) {
	}
	
	@Override
	public void doUnsubscribeFromPrices(PriceSubscriptionOptions options, String... symbols) {
	}
	
	@Override
	public void doConnect(ResultHandler<None> handler) {
	}
	
	@Override
	public void doDisconnect(ResultHandler<None> handler) {
	}
	
	@Override
	public LpAccountState accountState() {
		return new LpAccountState(utils.col.list(), utils.col.list());
	}
	
	
	public UnavailableLp(String lpid) {
		ReflectionUtils.inject(this, LiquidityProvider.class, "info", new LiquidityProviderInfo(
				lpid, UnavailableLp.class.getName(), "Unavailable LP" + lpid, "Unavailable LP", new HashMap<>()));
	}
	
}
