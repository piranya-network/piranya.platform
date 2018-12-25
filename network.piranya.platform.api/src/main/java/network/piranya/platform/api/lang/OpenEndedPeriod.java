package network.piranya.platform.api.lang;

public class OpenEndedPeriod {
	
	private final long value;
	public long value() { return value; }
	
	private final TimeWindowUnit unit;
	public TimeWindowUnit unit() { return unit; }
	
	private OpenEndedPeriod(long value, TimeWindowUnit unit) {
		this.value = value;
		this.unit = unit;
	}
	
	public static OpenEndedPeriod fixedStartTime(long startTimeUtc) {
		return new OpenEndedPeriod(startTimeUtc, null);
	}
	
	public static OpenEndedPeriod fixedStartTime(long backInterval, TimeWindowUnit unit) {
		return new OpenEndedPeriod(Period.getFixedStartTime(backInterval, unit), null);
	}
	
	public static OpenEndedPeriod slidingStartTime(long interval, TimeWindowUnit unit) {
		return new OpenEndedPeriod(interval, unit);
	}
	
}
