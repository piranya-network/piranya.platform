package network.piranya.platform.api.models.trading.liquidity;

import java.util.Map;

public class LpActivityInfo {
	
	public LpActivityInfo(Map<String, Object> data) {
		this.data = data;
	}
	
	private final Map<String, Object> data;
	public Map<String, Object> getData() {
		return data;
	}
	
	/** boolean */
	public static final String IS_CONNECTED = "core:IS_CONNECTED";
	/** long */
	public static final String LATENCY = "core:LATENCY";
	/** long */
	public static final String LAST_ACTIVITY_AT = "core:LAST_ACTIVITY_AT";
	/** int */
	public static final String TOTAL_INSTRUMENTS_COUNT = "core:TOTAL_INSTRUMENTS_COUNT";
	/** int */
	public static final String SUBSCRIBED_INSTRUMENTS_COUNT = "core:SUBSCRIBED_INSTRUMENTS_COUNT";
	
}
