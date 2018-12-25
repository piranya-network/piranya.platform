package network.piranya.platform.api.models.trading.price_feed;

import java.math.BigDecimal;
import java.util.List;

public class OrderBook implements PriceFeedRealtimeEntry {
	
	public OrderBook(String symbol, List<Entry> bids, List<Entry> asks, long time) {
		this.symbol = symbol;
		this.bids = bids;
		this.asks = asks;
		this.time = time;
	}
	
	private final String symbol;
	public String symbol() { return symbol; };
	
	private final List<Entry> bids;
	public List<Entry> bids() { return bids; }
	
	private final List<Entry> asks;
	public List<Entry> asks() { return asks; }
	
	private final long time;
	public long time() { return time; }
	
	
	public static class Entry {
		
		private final BigDecimal price;
		public BigDecimal price() { return price; }
		
		private final BigDecimal size;
		public BigDecimal size() { return size; }
		
		public Entry(BigDecimal price, BigDecimal size) {
			this.price = price;
			this.size = size;
		}
	}
	
	
	public static final String DATA_TYPE = "orderbook";
	
}
