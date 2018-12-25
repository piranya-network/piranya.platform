package network.piranya.platform.api.models.trading.ordering;

import java.io.Serializable;
import java.math.BigDecimal;

public class OrderSpec implements Serializable {
	
	private String symbol;
	public String symbol() {
		return symbol;
	}
	
	private OrderType orderType;
	public OrderType orderType() {
		return orderType;
	}
	
	private boolean isBuy;
	public boolean isBuy() {
		return isBuy;
	}
	
	private BigDecimal price;
	public BigDecimal price() {
		return price;
	}
	
	private BigDecimal size;
	public BigDecimal size() {
		return size;
	}
	
	private TimeInForce timeInForce;
	public TimeInForce timeInForce() {
		return timeInForce;
	}


	public OrderSpec() {
	}
	
	public OrderSpec(String symbol, boolean isBuy, BigDecimal size) {
		this(symbol, OrderType.MARKET, isBuy, new BigDecimal(0), size, new TimeInForce(TimeInForce.TimeInForceType.GTC));
	}
	
	public OrderSpec(String symbol, OrderType orderType, boolean isBuy, BigDecimal price, BigDecimal size) {
		this(symbol, orderType, isBuy, price, size, new TimeInForce(TimeInForce.TimeInForceType.GTC));
	}
	
	public OrderSpec(String symbol, OrderType orderType, boolean isBuy, BigDecimal price, BigDecimal size, TimeInForce timeInForce) {
		this.symbol = symbol;
		this.orderType = orderType;
		this.isBuy = isBuy;
		this.price = price;
		this.size = size;
		this.timeInForce = timeInForce;
	}
	
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof OrderSpec)) {
			return false;
		}
		
		OrderSpec other = (OrderSpec)o;
		return symbol().equals(other.symbol())
				&& orderType() == other.orderType()
				&& isBuy() == other.isBuy()
				&& price() == other.price()
				&& size() == other.size()
				&& timeInForce().equals(other.timeInForce());
	}
	
	@Override
	public String toString() {
		return String.format("OrderSpec(symbol: %s, type: %s %s, price: %s, size: %s, tif: %s)",
				symbol(), orderType(), isBuy() ? "Buy" : "Sell", price(), size(), timeInForce());
	}
	
	
	public OrderSpec updatePrice(BigDecimal newPrice) {
		return new OrderSpec(symbol(), orderType(), isBuy(), newPrice, size(), timeInForce());
	}
	
	public OrderSpec updateSize(BigDecimal newSize) {
		return new OrderSpec(symbol(), orderType(), isBuy(), price(), newSize, timeInForce());
	}
	
	public OrderSpec updateOrderType(OrderType newOrderType) {
		return new OrderSpec(symbol(), newOrderType, isBuy(), price(), size(), timeInForce());
	}
	
	public OrderSpec updateSymbol(String newSymbol) {
		return new OrderSpec(newSymbol, orderType(), isBuy(), price, size(), timeInForce());
	}
	
	
	private static final long serialVersionUID = ("urn:" + OrderSpec.class.getName()).hashCode();
	
}
