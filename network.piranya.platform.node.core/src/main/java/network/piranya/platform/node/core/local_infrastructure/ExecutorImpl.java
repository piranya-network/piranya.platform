package network.piranya.platform.node.core.local_infrastructure;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import network.piranya.platform.api.exceptions.PiranyaException;
import network.piranya.platform.node.api.local_infrastructure.concurrency.Executor;
import network.piranya.platform.node.utilities.RefImpl;
import network.piranya.platform.node.utilities.TimeService;

public class ExecutorImpl implements Executor {
	
	@Override
	public Future<?> scheduleAtFixedRate(Runnable task, long initialDelay, long delay, TimeUnit timeUnit) {
		return executor().scheduleAtFixedRate(task, initialDelay, delay, timeUnit);
	}
	
	@Override
	public Future<?> scheduleAtFixedRate(Supplier<Boolean> task, long initialDelay, long delay, TimeUnit timeUnit) {
		RefImpl<ScheduledFuture<?>> jobRef = new RefImpl<>();
		jobRef.set(executor().scheduleAtFixedRate(() -> {
			if (!task.get()) {
				jobRef.get().cancel(false);
			}
		}, initialDelay, delay, timeUnit));
		return jobRef.get();
	}
	
	@Override
	public Future<?> scheduleConveniently(Supplier<Boolean> task, long initialDelay, long delay, TimeUnit timeUnit, int priority) {
		// for now simplified. in the future must factor in available resources and busy-ness
		return scheduleAtFixedRate(task, initialDelay, delay, timeUnit);
	}
	
	@Override
	public Future<?> scheduleAfter(Runnable task, long delay, TimeUnit timeUnit) {
		return executor().schedule(task, delay, timeUnit);
	}
	
	@Override
	public Future<?> execute(Runnable task) {
		return executor().submit(task);
	}
	
	@Override
	public Future<?> submit(Runnable task) {
		return executor().submit(task);
	}
	
	@Override
	public boolean waitUntil(Supplier<Boolean> predicate, long timeout) {
		try {
			long startTime = TimeService.now();
			boolean result = false;
			while (!(result = predicate.get()) && TimeService.now() - startTime < timeout) {
				Thread.sleep(5);
			}
			return result;
		} catch (Throwable ex) {
			throw new PiranyaException(ex);
		}
	}
	
	@Override
	public void dispose() {
		executor().shutdownNow();
	}
	
	
	public ExecutorImpl(Config config) {
		this.executor = Executors.newScheduledThreadPool(config.threadsAmount());
	}
	
	private final ScheduledExecutorService executor;
	protected ScheduledExecutorService executor() {
		return executor;
	}
	
}
