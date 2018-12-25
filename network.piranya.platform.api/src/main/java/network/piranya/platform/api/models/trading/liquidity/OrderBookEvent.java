package network.piranya.platform.api.models.trading.liquidity;

import java.math.BigDecimal;
import java.util.List;

import network.piranya.platform.api.models.trading.ExecutionEvent;
import network.piranya.platform.api.models.trading.Instrument;

public class OrderBookEvent implements ExecutionEvent {
	
	private final Instrument instrument;
	public Instrument instrument() {
		return instrument;
	}
	
	private final List<BigDecimal> bids;
	public List<BigDecimal> bids() {
		return bids;
	}
	
	private final List<BigDecimal> asks;
	public List<BigDecimal> asks() {
		return asks;
	}
	
	public OrderBookEvent(Instrument instrument, List<BigDecimal> bids, List<BigDecimal> asks) {
		this.instrument = instrument;
		this.bids = bids;
		this.asks = asks;
	}
	
}
