package network.piranya.platform.api.models.trading.liquidity;

import network.piranya.platform.api.models.trading.ExchangeTrade;
import network.piranya.platform.api.models.trading.ExecutionEvent;

public class ExchangeTradeEvent implements ExecutionEvent {
	
	private final ExchangeTrade trade;
	public ExchangeTrade trade() { return trade; }
	
	public ExchangeTradeEvent(ExchangeTrade trade) {
		this.trade = trade;
	}
	
}
