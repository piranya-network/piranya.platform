package network.piranya.platform.api.models.trading.liquidity;

public class LiquidityProviderRef {
	
	private final String liquidityProviderId;
	public String liquidityProviderId() {
		return liquidityProviderId;
	}
	
	public LiquidityProviderRef(String liquidityProviderId) {
		this.liquidityProviderId = liquidityProviderId;
	}
	
	@Override
	public int hashCode() {
		return liquidityProviderId().hashCode();
	}
	
	@Override
	public String toString() {
		return liquidityProviderId();
	}
	
	@Override
	public boolean equals(Object o) {
		return o instanceof LiquidityProviderRef && liquidityProviderId().equals(((LiquidityProviderRef)o).liquidityProviderId());
	}
	
}
