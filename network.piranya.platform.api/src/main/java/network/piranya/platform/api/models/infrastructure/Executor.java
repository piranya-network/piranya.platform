package network.piranya.platform.api.models.infrastructure;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public interface Executor {
	
	void scheduleAtFixedRate(Runnable task, long initialDelay, long delay, TimeUnit timeUnit);
	
	void scheduleAtFixedRate(Supplier<Boolean> task, long initialDelay, long delay, TimeUnit timeUnit);
	
	void scheduleAfter(Runnable task, long delay, TimeUnit timeUnit);
	
	void execute(Runnable task);
	
	long now();
	
}
