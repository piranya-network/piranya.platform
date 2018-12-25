package network.piranya.platform.api.models.trading.liquidity;

import network.piranya.platform.api.models.trading.ExecutionEvent;
import network.piranya.platform.api.models.trading.Instrument;
import network.piranya.platform.api.models.trading.Quote;

public class QuoteEvent implements ExecutionEvent {
	
	private final Quote quote;
	public Quote quote() {
		return quote;
	}
	
	public Instrument instrument() {
		return quote.instrument();
	}
	
	public QuoteEvent(Quote quote) {
		this.quote = quote;
	}
	
}
