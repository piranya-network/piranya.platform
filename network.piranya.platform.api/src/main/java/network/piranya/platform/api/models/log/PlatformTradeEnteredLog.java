package network.piranya.platform.api.models.log;

import network.piranya.platform.api.models.trading.filling.Fill;

public class PlatformTradeEnteredLog extends ActivityLog {
	
	private final Fill fill;
	public Fill getFill() {
		return fill;
	}
	
	public PlatformTradeEnteredLog(long time, Fill fill) {
		super(time);
		this.fill = fill;
	}
	
}
