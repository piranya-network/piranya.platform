package network.piranya.platform.api.models.trading.ordering;

import java.io.Serializable;

public class TimeInForce implements Serializable {
	
	public static enum TimeInForceType { GTC, GTD, TIMEOUT }
	
	
	public TimeInForce() {
		this.type = TimeInForceType.GTC;
		this.timeout = null;
	}
	
	public TimeInForce(TimeInForceType type) {
		this.type = type;
		this.timeout = null;
	}
	
	public TimeInForce(long timeout) {
		this.timeout = timeout;
		this.type = TimeInForceType.TIMEOUT;
	}
	
	private final TimeInForceType type;
	public TimeInForceType type() {
		return type;
	}
	
	private final Long timeout;
	public Long timeout() {
		return timeout;
	}
	
	
	@Override
	public String toString() {
		if (timeout() != null) {
			return Long.toString(timeout());
		} else {
			return type().name();
		}
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TimeInForce)) {
			return false;
		}
		
		TimeInForce o = (TimeInForce)obj;
		return type() == o.type() && (timeout() == o.timeout() || (timeout() != null && o.timeout() != null && o.timeout().equals(o.timeout())));
	}

	private static final long serialVersionUID = ("urn:" + TimeInForce.class.getName()).hashCode();
	
}
