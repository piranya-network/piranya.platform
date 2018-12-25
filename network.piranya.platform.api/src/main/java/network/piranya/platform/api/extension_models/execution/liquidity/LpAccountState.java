package network.piranya.platform.api.extension_models.execution.liquidity;

import java.util.List;

public class LpAccountState {
	
	private final List<LpTrade> trades;
	public List<LpTrade> trades() {
		return trades;
	}
	
	private final List<LpPendingOrder> pendingOrders;
	public List<LpPendingOrder> pendingOrders() {
		return pendingOrders;
	}
	
	public LpAccountState(List<LpTrade> trades, List<LpPendingOrder> pendingOrders) {
		this.trades = trades;
		this.pendingOrders = pendingOrders;
	}
	
}
