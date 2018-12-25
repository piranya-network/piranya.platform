package network.piranya.platform.node.core.execution.storage;

import java.math.BigDecimal;

public class Quote {
	
	private final String symbol;
	public String symbol() {
		return symbol;
	}
	
	private final BigDecimal bid;
	public BigDecimal bid() {
		return bid;
	}
	
	private final BigDecimal ask;
	public BigDecimal ask() {
		return ask;
	}
	
	public Quote(String symbol, BigDecimal bid, BigDecimal ask) {
		this.symbol = symbol;
		this.bid = bid;
		this.ask = ask;
	}
	
}
