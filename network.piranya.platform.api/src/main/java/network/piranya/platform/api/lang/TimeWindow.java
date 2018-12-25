package network.piranya.platform.api.lang;

public class TimeWindow {
	
	private final long startTime;
	public long startTime() { return startTime; }
	
	private final long endTime;
	public long endTime() { return endTime; }

	public TimeWindow(long startTime, long endTime) {
		this.startTime = startTime;
		this.endTime = endTime;
	}
	
}
