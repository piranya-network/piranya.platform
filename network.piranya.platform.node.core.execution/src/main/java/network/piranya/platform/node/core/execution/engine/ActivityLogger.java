package network.piranya.platform.node.core.execution.engine;

import java.io.DataOutput;
import java.io.IOException;
import java.util.function.Consumer;

import network.piranya.platform.api.models.log.ActivityLog;
import network.piranya.platform.api.models.log.GeneralActivityLog;
import network.piranya.platform.api.models.log.LpFillLog;
import network.piranya.platform.api.models.log.LpOrderAcceptedLog;
import network.piranya.platform.api.models.log.LpOrderCancelLog;
import network.piranya.platform.api.models.log.LpOrderCancelledLog;
import network.piranya.platform.api.models.log.LpOrderPlaceLog;
import network.piranya.platform.api.models.log.LpOrderRejectedLog;
import network.piranya.platform.api.models.log.PlatformOrderCancelledLog;
import network.piranya.platform.api.models.log.PlatformOrderPlacedLog;
import network.piranya.platform.api.models.log.PlatformTradeEnteredLog;
import network.piranya.platform.node.api.local_infrastructure.concurrency.Executor;
import network.piranya.platform.node.api.local_infrastructure.storage.LocalStorage;
import network.piranya.platform.node.api.local_infrastructure.storage.PersistentQueue;
import network.piranya.platform.node.utilities.BitUtils;
import network.piranya.platform.node.utilities.Encoder;
import network.piranya.platform.node.utilities.EventsSubscriptionSupport;

public class ActivityLogger {
	
	public void log(ActivityLog l) {
		executor().execute(() -> {
			queue().append(encoder().encode(l, out -> prependTime(l, out)));
			subscriptionSupport.publish(l, true);
		});
	}
	
	public void query(long startTime, long endTime, Consumer<ActivityLog> consumer) {
		byte[] readingBuffer = new byte[100_000];
		queue().read(readingBuffer, len -> {
			long time = BitUtils.longFromByteArray(readingBuffer, 0);
			if (time >= startTime && time <= endTime) {
				consumer.accept(encoder().decode(readingBuffer, 8, len - 8));
			}
		});
	}
	
	public void subscribe(Consumer<ActivityLog> subscriber) {
		subscriptionSupport.subscribe(subscriber);
	}
	
	public void unsubscribe(Consumer<ActivityLog> subscriber) {
		subscriptionSupport.unsubscribe(subscriber);
	}
	
	public void dispose() {
		queue().release(this);
	}
	
	
	protected void prependTime(ActivityLog l, DataOutput output) {
		try { output.writeLong(l.getTime()); }
		catch (IOException ex) { throw new RuntimeException(ex); }
	}
	
	
	public ActivityLogger(LocalStorage localStorage, Executor executor) {
		this.queue = localStorage.persistentQueue("system#activity_log", new PersistentQueue.Config(), this);
		this.executor = executor;
		
		this.encoder = new Encoder();
		encoder().registerDataType(GeneralActivityLog.class, (short)100);
		encoder().registerDataType(PlatformOrderPlacedLog.class, (short)200);
		encoder().registerDataType(PlatformOrderCancelledLog.class, (short)201);
		encoder().registerDataType(PlatformTradeEnteredLog.class, (short)202);
		encoder().registerDataType(LpOrderPlaceLog.class, (short)300);
		encoder().registerDataType(LpOrderAcceptedLog.class, (short)301);
		encoder().registerDataType(LpOrderRejectedLog.class, (short)302);
		encoder().registerDataType(LpOrderCancelLog.class, (short)303);
		encoder().registerDataType(LpOrderCancelledLog.class, (short)304);
		encoder().registerDataType(LpFillLog.class, (short)305);
	}
	
	/// a constructor for those want to override activity logger behavior
	protected ActivityLogger() {
		this.queue = null;
		this.executor = null;
		this.encoder = null;
	}
	
	private final PersistentQueue queue;
	protected PersistentQueue queue() { return queue; }
	
	private final Encoder encoder;
	protected Encoder encoder() { return encoder; }
	
	private final Executor executor;
	protected Executor executor() { return executor; }
	
	private final EventsSubscriptionSupport<ActivityLog> subscriptionSupport = new EventsSubscriptionSupport<>();
	protected EventsSubscriptionSupport<ActivityLog> subscriptionSupport() { return subscriptionSupport; }
	
}
