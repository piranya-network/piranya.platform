package network.piranya.platform.api.models.trading.price_feed;

import java.math.BigDecimal;

public class ExchangeTrade implements PriceFeedRealtimeEntry {
	
	public ExchangeTrade(String symbol, BigDecimal price, BigDecimal size, String exchangeTradeId, long time) {
		this.symbol = symbol;
		this.exchangeTradeId = exchangeTradeId;
		this.price = price;
		this.size = size;
		this.time = time;
	}
	
	private final String symbol;
	public String symbol() { return symbol; }
	
	private final BigDecimal price;
	public BigDecimal price() { return price; }
	
	private final BigDecimal size;
	public BigDecimal size() { return size; }
	
	private final String exchangeTradeId;
	public String exchangeTradeId() { return exchangeTradeId; }
	
	private final long time;
	public long time() { return time; }
	
	
	public static final String DATA_TYPE = "exchange_trade";
	
}
