package network.piranya.platform.node.utilities;

import java.util.function.Supplier;

public abstract class WaitingUtils {
	
	public static boolean waitUntil(long time, int stepTime, Supplier<Boolean> condition) {
		long cnt = 0;
		while (cnt++ <= time) {
			try {
				if (condition.get() == true) return true;
				Thread.sleep(stepTime);
			} catch (Exception e) {}
		}
		return false;
	}
	
	
	private WaitingUtils() { }
	
}
