package network.piranya.platform.api.models.log;

public interface ActivityLogStore extends ActivityLogReader {
	
	void log(ActivityLog l);
	
}
