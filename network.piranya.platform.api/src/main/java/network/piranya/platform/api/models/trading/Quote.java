package network.piranya.platform.api.models.trading;

import java.math.BigDecimal;
import network.piranya.platform.api.lang.Optional;

public class Quote {
	
	private final Instrument instrument;
	public Instrument instrument() {
		return instrument;
	}
	
	private final BigDecimal bid;
	public BigDecimal bid() {
		return bid;
	}
	
	private final BigDecimal ask;
	public BigDecimal ask() {
		return ask;
	}
	
	private final Optional<OrderBook> orderBook;
	public Optional<OrderBook> orderBook() {
		return orderBook;
	}
	
	public Quote(Instrument instrument, BigDecimal bid, BigDecimal ask) {
		this.instrument = instrument;
		this.bid = bid;
		this.ask = ask;
		this.orderBook = Optional.empty();
	}
	
	public Quote(Instrument instrument, OrderBook orderBook) {
		this.instrument = instrument;
		this.orderBook = Optional.of(orderBook);
		this.bid = orderBook.bids()[0].price();
		this.ask = orderBook.asks()[0].price();
	}
	
	public Quote(Instrument instrument, Quote q) {
		this.instrument = instrument;
		this.orderBook = q.orderBook();
		this.bid = q.bid();
		this.ask = q.ask();
	}
	
}
