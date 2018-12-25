package network.piranya.platform.api.models.log;

import network.piranya.platform.api.models.trading.ordering.PendingOrder;

public class PlatformOrderCancelledLog extends ActivityLog {
	
	private final PendingOrder order;
	public PendingOrder getOrder() {
		return order;
	}
	
	public PlatformOrderCancelledLog(long time, PendingOrder order) {
		super(time);
		this.order = order;
	}
	
}
