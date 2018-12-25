package network.piranya.platform.api.models.log;

import network.piranya.platform.api.models.trading.ordering.PendingOrder;

public class PlatformOrderPlacedLog extends ActivityLog {
	
	private final PendingOrder order;
	public PendingOrder getOrder() {
		return order;
	}
	
	public PlatformOrderPlacedLog(long time, PendingOrder order) {
		super(time);
		this.order = order;
	}
	
}
