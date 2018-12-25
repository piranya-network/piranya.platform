package network.piranya.platform.api.extension_models.analytics;

import network.piranya.platform.api.extension_models.Parameters;
import network.piranya.platform.api.models.info.MarketInfoProvider;
import network.piranya.platform.api.models.log.ActivityLog;

public interface AnalyticalQuery {
	
	void init(Parameters params, MarketInfoProvider marketInfo);
	
	boolean filter(ActivityLog log);
	
	void accept(ActivityLog log);
	
	Object getQueryResult();
	
}
