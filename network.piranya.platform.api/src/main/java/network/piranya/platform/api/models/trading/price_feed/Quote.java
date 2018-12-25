package network.piranya.platform.api.models.trading.price_feed;

import java.math.BigDecimal;

public class Quote implements PriceFeedRealtimeEntry {
	
	private final String symbol;
	public String symbol() { return symbol; }
	
	private final BigDecimal bid;
	public BigDecimal bid() { return bid; }
	
	private final BigDecimal ask;
	public BigDecimal ask() { return ask; }
	
	private final BigDecimal bidSize;
	public BigDecimal bidSize() { return bidSize; }
	
	private final BigDecimal askSize;
	public BigDecimal askSize() { return askSize; }
	
	private final long time;
	public long time() { return time; }
	
	public Quote(String symbol, BigDecimal bid, BigDecimal ask, BigDecimal bidSize, BigDecimal askSize, long time) {
		this.symbol = symbol;
		this.bid = bid;
		this.ask = ask;
		this.bidSize = bidSize;
		this.askSize = askSize;
		this.time = time;
	}
	
	
	public static final String DATA_TYPE = "quote";
	
}
