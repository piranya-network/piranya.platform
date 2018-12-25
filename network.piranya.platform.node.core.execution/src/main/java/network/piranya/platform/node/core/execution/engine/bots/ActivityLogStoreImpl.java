package network.piranya.platform.node.core.execution.engine.bots;

import java.util.function.Consumer;

import network.piranya.platform.api.models.log.ActivityLog;
import network.piranya.platform.api.models.log.ActivityLogStore;
import network.piranya.platform.node.core.execution.engine.ActivityLogger;
import network.piranya.platform.node.utilities.EventsSubscriptionSupport;

public class ActivityLogStoreImpl implements ActivityLogStore {
	
	@Override
	public void log(ActivityLog l) {
		activityLogger().log(l);
	}
	
	@Override
	public void query(long startTime, long endTime, Consumer<ActivityLog> processor) {
		activityLogger().query(startTime, endTime, processor);
	}
	
	public void subscribe(Consumer<ActivityLog> subscriber) {
		eventsSubscriptionSupport.subscribe(subscriber);
	}
	
	public void unsubscribe(Consumer<ActivityLog> subscriber) {
		eventsSubscriptionSupport.unsubscribe(subscriber);
	}
	
	protected void onActivityLog(ActivityLog log) {
		eventsSubscriptionSupport.publish(log, true);
	}
	
	public void dispose() {
		activityLogger().unsubscribe(this.activityLogSubscriber);
	}
	
	
	public ActivityLogStoreImpl(ActivityLogger activityLogger) {
		this.activityLogger = activityLogger;
		activityLogger().subscribe(this.activityLogSubscriber);
	}
	
	private final Consumer<ActivityLog> activityLogSubscriber = this::onActivityLog;
	
	private final ActivityLogger activityLogger;
	protected ActivityLogger activityLogger() { return activityLogger; }
	
	private final EventsSubscriptionSupport<ActivityLog> eventsSubscriptionSupport = new EventsSubscriptionSupport<>();
	
}
