package network.piranya.platform.api.lang;

public enum TimeWindowUnit {
	
	MINUTE(1000L * 60L), HOUR(1000L * 60L * 60L), DAY(1000L * 60L * 60L * 24L), WEEK(1000L * 60L * 60L * 24L * 7L), MONTH(1000L * 60L * 60L * 24L * 31L);
	
	
	private TimeWindowUnit(long inMillis) {
		this.inMillis = inMillis;
	}
	
	private final long inMillis;
	public long inMillis() {
		return inMillis;
	}
	
}
