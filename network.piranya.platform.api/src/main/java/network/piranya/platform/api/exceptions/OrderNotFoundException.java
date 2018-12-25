package network.piranya.platform.api.exceptions;

import network.piranya.platform.api.models.trading.ordering.OrderRef;

public class OrderNotFoundException extends PiranyaException {
	
	public OrderNotFoundException(OrderRef orderRef) {
		super(String.format("Order '%s' was not found", orderRef));
	}
	
	public OrderNotFoundException(String message) {
		super(message);
	}
	
	
	private static final long serialVersionUID = ("urn:" + OrderNotFoundException.class.getName()).hashCode();
	
}
