package network.piranya.platform.node.api.local_infrastructure.concurrency;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public interface Executor {
	
	Future<?> scheduleAtFixedRate(Runnable task, long initialDelay, long delay, TimeUnit timeUnit);

	Future<?> scheduleAtFixedRate(Supplier<Boolean> task, long initialDelay, long delay, TimeUnit timeUnit);

	Future<?> scheduleConveniently(Supplier<Boolean> task, long initialDelay, long delay, TimeUnit timeUnit, int priority);
	
	Future<?> scheduleAfter(Runnable task, long delay, TimeUnit timeUnit);
	
	Future<?> execute(Runnable task);
	
	Future<?> submit(Runnable task);

	/** returns true if predicate condition was met. false if timedout */
	boolean waitUntil(Supplier<Boolean> predicate, long timeout);
	
	void dispose();
	
	
	public static final int CRITICAL_PRIORITY = 100;
	public static final int LOWEST_PRIORITY = 900;
	
	
	public static class Config {
		
		public Config(int threadsAmount) {
			this.threadsAmount = threadsAmount;
		}
		
		private final int threadsAmount;
		public int threadsAmount() { return threadsAmount; }
	}
	
}
