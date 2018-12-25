package network.piranya.platform.api.exceptions;

import network.piranya.platform.api.models.trading.Instrument;

public class PricesNotAvailableException extends PiranyaException {
	
	public PricesNotAvailableException(Instrument instrument) {
		super(String.format("Prices for instrument '%s' are not available", instrument));
	}
	
	public PricesNotAvailableException(String message) {
		super(message);
	}
	
	
	private static final long serialVersionUID = ("urn:" + PricesNotAvailableException.class.getName()).hashCode();
	
}
