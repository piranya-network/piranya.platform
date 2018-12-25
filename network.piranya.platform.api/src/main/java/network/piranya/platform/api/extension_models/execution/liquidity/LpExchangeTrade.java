package network.piranya.platform.api.extension_models.execution.liquidity;

import network.piranya.platform.api.models.trading.ExchangeTrade;

public class LpExchangeTrade implements LpEvent {
	
	private final ExchangeTrade trade;
	public ExchangeTrade trade() { return trade; }
	
	public LpExchangeTrade(ExchangeTrade trade) {
		this.trade = trade;
	}
	
}
