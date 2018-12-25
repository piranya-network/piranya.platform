package network.piranya.platform.api.extension_models;

import network.piranya.platform.api.models.trading.liquidity.LiquidityProvidersAdmin;

public interface ManagerialExtensionContext extends ExtensionContext {
	
	LiquidityProvidersAdmin liquidityProvidersAdmin();
	
}
