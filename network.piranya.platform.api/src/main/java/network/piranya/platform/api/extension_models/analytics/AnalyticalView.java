package network.piranya.platform.api.extension_models.analytics;

import network.piranya.platform.api.extension_models.Parameters;
import network.piranya.platform.api.models.analytics.AnalyticalViewData;
import network.piranya.platform.api.models.info.MarketInfoProvider;
import network.piranya.platform.api.models.log.ActivityLog;

public interface AnalyticalView {
	
	void init(Parameters params, MarketInfoProvider marketInfo);
	
	boolean filter(ActivityLog log);
	
	void accept(ActivityLog log);
	
	void forget(ActivityLog log);
	
	AnalyticalViewData<?> data();
	
}
