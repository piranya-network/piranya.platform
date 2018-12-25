package network.piranya.platform.node.core.execution.infrastructure.serialization.json;

import java.math.BigDecimal;
import java.util.List;

public class OrderBookEntry {
	
	private String symbol_id;
	public String getSymbol_id() {
		return symbol_id;
	}
	public void setSymbol_id(String symbolId) {
		this.symbol_id = symbolId;
	}
	
	private String time_exchange;
	public String getTime_exchange() {
		return time_exchange;
	}
	public void setTime_exchange(String timeExchange) {
		this.time_exchange = timeExchange;
	}
	
	private String time_coinapi;
	public String getTime_coinapi() {
		return time_coinapi;
	}
	public void setTime_coinapi(String timeCoinapi) {
		this.time_coinapi = timeCoinapi;
	}
	
	private List<Entry> asks;
	public List<Entry> getAsks() {
		return asks;
	}
	public void setAsks(List<Entry> asks) {
		this.asks = asks;
	}
	
	private List<Entry> bids;
	public List<Entry> getBids() {
		return bids;
	}
	public void setBids(List<Entry> bids) {
		this.bids = bids;
	}
	
	
	public static class Entry {
		
		private BigDecimal price;
		public BigDecimal getPrice() {
			return price;
		}
		public void setPrice(BigDecimal price) {
			this.price = price;
		}
		
		private BigDecimal size;
		public BigDecimal getSize() {
			return size;
		}
		public void setSize(BigDecimal size) {
			this.size = size;
		}
		
	}
	
}
