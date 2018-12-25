package network.piranya.platform.api.models.log;

import java.util.function.Consumer;

public interface ActivityLogReader {
	
	void query(long startTime, long endTime, Consumer<ActivityLog> processor);
	
	void subscribe(Consumer<ActivityLog> subscriber);
	void unsubscribe(Consumer<ActivityLog> subscriber);
	
}
