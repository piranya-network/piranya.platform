package network.piranya.platform.api.extension_models.execution.liquidity;

import network.piranya.platform.api.models.trading.Quote;

public class LpQuote implements LpEvent {
	
	private final Quote quote;
	public Quote quote() {
		return quote;
	}
	
	public LpQuote(Quote quote) {
		this.quote = quote;
	}
	
}
