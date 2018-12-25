package network.piranya.platform.node.utilities;

public abstract class TimeService {
	
	public static long now() {
		return System.currentTimeMillis();
	}
	
	
	public static long inDays(long days) {
		return inHours(24);
	}
	
	public static long inHours(long hours) {
		return (hours * 1000L * 60L * 60L) + now();
	}
	
	
	private TimeService() { }
	
}
