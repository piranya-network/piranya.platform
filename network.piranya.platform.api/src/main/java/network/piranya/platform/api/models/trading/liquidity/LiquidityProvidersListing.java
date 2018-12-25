package network.piranya.platform.api.models.trading.liquidity;

public interface LiquidityProvidersListing<Context> {
	
	LiquidityProvider<Context> get(String liquidityProviderId);
	
}
