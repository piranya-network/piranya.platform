package network.piranya.platform.api.models.trading;

import java.math.BigDecimal;

public class OrderBook {
	
	private final Entry[] bids;
	public Entry[] bids() {
		return bids;
	}
	
	private final Entry[] asks;
	public Entry[] asks() {
		return asks;
	}
	
	public OrderBook(Entry[] bids, Entry[] asks) {
		this.bids = bids;
		this.asks = asks;
	}
	
	
	public OrderBook updateFrom(OrderBook updates) {
		Entry[] bids = new Entry[Math.max(bids().length, updates.bids().length)];
		Entry[] asks = new Entry[Math.max(bids().length, updates.bids().length)];
		
		for (int i = 0; i < bids().length; i++) {
			bids[i] = bids()[i];
		}
		for (int i = 0; i < asks().length; i++) {
			asks[i] = asks()[i];
		}
		
		for (int i = 0; i < updates.bids().length; i++) {
			Entry bid = updates.bids()[i];
			if (bid != null) {
				bids[i] = bid;
			}
		}
		for (int i = 0; i < updates.asks().length; i++) {
			Entry ask = updates.asks()[i];
			if (ask != null) {
				asks[i] = ask;
			}
		}
		
		return new OrderBook(bids, asks);
	}
	
	
	public static class Entry {
		
		private final BigDecimal price;
		public BigDecimal price() {
			return price;
		}
		
		private final BigDecimal size;
		public BigDecimal size() {
			return size;
		}
		
		public Entry(BigDecimal price, BigDecimal size) {
			this.price = price;
			this.size = size;
		}
	}
	
	
	public static final OrderBook EMPTY = new OrderBook(new Entry[0], new Entry[0]);
	
}
