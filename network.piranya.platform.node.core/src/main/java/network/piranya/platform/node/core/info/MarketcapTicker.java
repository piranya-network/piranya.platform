package network.piranya.platform.node.core.info;

import java.math.BigDecimal;
import java.util.Map;

public class MarketcapTicker {
	
	private Map<String, Entry> data;
	public Map<String, Entry> getData() {
		return data;
	}
	public void setData(Map<String, Entry> data) {
		this.data = data;
	}
	
	private Metadata metadata;
	public Metadata getMetadata() {
		return metadata;
	}
	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}
	
	public static class Entry {
		
		private String id;
		private String name;
		private String symbol;
		private String website_slug;
		private int rank;
		private BigDecimal circulating_supply;
		private BigDecimal total_supply;
		private BigDecimal max_supply;
		private Map<String, Quote> quotes;
		private long last_updated;
		
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getSymbol() {
			return symbol;
		}
		public void setSymbol(String symbol) {
			this.symbol = symbol;
		}
		public String getWebsite_slug() {
			return website_slug;
		}
		public void setWebsite_slug(String website_slug) {
			this.website_slug = website_slug;
		}
		public int getRank() {
			return rank;
		}
		public void setRank(int rank) {
			this.rank = rank;
		}
		public BigDecimal getCirculating_supply() {
			return circulating_supply;
		}
		public void setCirculating_supply(BigDecimal circulating_supply) {
			this.circulating_supply = circulating_supply;
		}
		public BigDecimal getTotal_supply() {
			return total_supply;
		}
		public void setTotal_supply(BigDecimal total_supply) {
			this.total_supply = total_supply;
		}
		public BigDecimal getMax_supply() {
			return max_supply;
		}
		public void setMax_supply(BigDecimal max_supply) {
			this.max_supply = max_supply;
		}
		public Map<String, Quote> getQuotes() {
			return quotes;
		}
		public void setQuotes(Map<String, Quote> quotes) {
			this.quotes = quotes;
		}
		public long getLast_updated() {
			return last_updated;
		}
		public void setLast_updated(long last_updated) {
			this.last_updated = last_updated;
		}
		
	}
	
	public static class Quote {
		
		private BigDecimal price;
		private BigDecimal volume_24h;
		private BigDecimal market_cap;
		private BigDecimal percent_change_1h;
		private BigDecimal percent_change_24h;
		private BigDecimal percent_change_7d;
		
		public BigDecimal getPrice() {
			return price;
		}
		public void setPrice(BigDecimal price) {
			this.price = price;
		}
		public BigDecimal getVolume_24h() {
			return volume_24h;
		}
		public void setVolume_24h(BigDecimal volume_24h) {
			this.volume_24h = volume_24h;
		}
		public BigDecimal getMarket_cap() {
			return market_cap;
		}
		public void setMarket_cap(BigDecimal market_cap) {
			this.market_cap = market_cap;
		}
		public BigDecimal getPercent_change_1h() {
			return percent_change_1h;
		}
		public void setPercent_change_1h(BigDecimal percent_change_1h) {
			this.percent_change_1h = percent_change_1h;
		}
		public BigDecimal getPercent_change_24h() {
			return percent_change_24h;
		}
		public void setPercent_change_24h(BigDecimal percent_change_24h) {
			this.percent_change_24h = percent_change_24h;
		}
		public BigDecimal getPercent_change_7d() {
			return percent_change_7d;
		}
		public void setPercent_change_7d(BigDecimal percent_change_7d) {
			this.percent_change_7d = percent_change_7d;
		}
		
	}
	
	public static class Metadata {
		
		private long timestamp;
		private int num_cryptocurrencies;
		private String error;
		
		public long getTimestamp() {
			return timestamp;
		}
		public void setTimestamp(long timestamp) {
			this.timestamp = timestamp;
		}
		public int getNum_cryptocurrencies() {
			return num_cryptocurrencies;
		}
		public void setNum_cryptocurrencies(int num_cryptocurrencies) {
			this.num_cryptocurrencies = num_cryptocurrencies;
		}
		public String getError() {
			return error;
		}
		public void setError(String error) {
			this.error = error;
		}
		
	}
	
}
