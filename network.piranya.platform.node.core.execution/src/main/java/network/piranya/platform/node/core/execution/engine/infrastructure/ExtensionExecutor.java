package network.piranya.platform.node.core.execution.engine.infrastructure;

import static network.piranya.platform.node.utilities.CollectionUtils.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import network.piranya.platform.node.api.local_infrastructure.concurrency.Executor;
import network.piranya.platform.node.utilities.TimeService;

public class ExtensionExecutor implements network.piranya.platform.api.models.infrastructure.Executor {
	
	@Override
	public void scheduleAtFixedRate(Runnable task, long initialDelay, long delay, TimeUnit timeUnit) {
		futures().put(executor().scheduleAtFixedRate(task, initialDelay, delay, timeUnit), true);
	}
	
	@Override
	public void scheduleAtFixedRate(Supplier<Boolean> task, long initialDelay, long delay, TimeUnit timeUnit) {
		futures().put(executor().scheduleAtFixedRate(task, initialDelay, delay, timeUnit), true);
	}
	
	@Override
	public void scheduleAfter(Runnable task, long delay, TimeUnit timeUnit) {
		futures().put(executor().scheduleAfter(task, delay, timeUnit), true);
	}
	
	@Override
	public void execute(Runnable task) {
		futures().put(executor().execute(task), true);
	}
	
	@Override
	public long now() {
		return TimeService.now();
	}
	
	public void dispose() {
		cancel(checkCompletedFuture());
		foreach(futures().keySet(), f -> cancel(f));
	}
	
	protected void checkCompleted() {
		foreach(filter(futures().keySet(), f -> f.isDone()), f -> futures().remove(f));
	}
	
	protected void cancel(Future<?> future) {
		try { future.cancel(true); }
		catch (Throwable ex) { }
	}
	
	
	public ExtensionExecutor(Executor executor) {
		this.executor = executor;
		
		this.checkCompletedFuture = executor.scheduleAtFixedRate(this::checkCompleted, 1000, 1000, TimeUnit.MILLISECONDS);
	}
	
	private final Executor executor;
	protected Executor executor() { return executor; }
	
	private final ConcurrentMap<Future<?>, Boolean> futures = new ConcurrentHashMap<>();
	protected ConcurrentMap<Future<?>, Boolean> futures() { return futures; }
	
	private final Future<?> checkCompletedFuture;
	protected Future<?> checkCompletedFuture() { return checkCompletedFuture; }
	
}
