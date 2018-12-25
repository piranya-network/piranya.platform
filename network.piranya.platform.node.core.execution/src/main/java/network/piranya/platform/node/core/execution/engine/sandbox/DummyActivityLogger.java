package network.piranya.platform.node.core.execution.engine.sandbox;

import java.util.function.Consumer;

import network.piranya.platform.api.models.log.ActivityLog;
import network.piranya.platform.node.core.execution.engine.ActivityLogger;

public class DummyActivityLogger extends ActivityLogger {
	
	@Override
	public void log(ActivityLog l) {
		subscriptionSupport().publish(l, true);
	}
	
	@Override
	public void query(long startTime, long endTime, Consumer<ActivityLog> consumer) {
	}
	
	@Override
	public void subscribe(Consumer<ActivityLog> subscriber) {
		subscriptionSupport().subscribe(subscriber);
	}
	
	@Override
	public void unsubscribe(Consumer<ActivityLog> subscriber) {
		subscriptionSupport().unsubscribe(subscriber);
	}
	
}
