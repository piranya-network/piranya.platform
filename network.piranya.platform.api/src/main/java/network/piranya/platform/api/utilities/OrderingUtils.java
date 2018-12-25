package network.piranya.platform.api.utilities;

import java.math.BigDecimal;

import network.piranya.platform.api.models.trading.ordering.OrderType;

public class OrderingUtils {
	
	public boolean matches(BigDecimal orderPrice, OrderType orderType, boolean isBuy, BigDecimal bid, BigDecimal ask) {
		return matches(orderPrice, orderType, isBuy, isBuy ? ask : bid);
	}
	
	public boolean matches(BigDecimal orderPrice, OrderType orderType, boolean isBuy, BigDecimal marketPrice) {
		int diff = marketPrice.compareTo(orderPrice);
		return (isBuy && ((orderType == OrderType.LIMIT && diff <= 0) || (orderType == OrderType.STOP && diff >= 0)))
				|| (!isBuy && ((orderType == OrderType.LIMIT && diff >= 0) || (orderType == OrderType.STOP && diff <= 0)));
	}
	
	
	protected OrderingUtils() { }
	
}
