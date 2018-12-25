package network.piranya.platform.api.models.analytics;

import network.piranya.platform.api.extension_models.Parameters;
import network.piranya.platform.api.lang.OpenEndedPeriod;
import network.piranya.platform.api.lang.Period;
import network.piranya.platform.api.lang.ResultHandler;

public interface Analytics {
	
	<ViewDataType> void view(String analyticalViewId, String analyticalViewTypeId, boolean persistent, OpenEndedPeriod startPeriod, Parameters params,
			ResultHandler<AnalyticalViewData<ViewDataType>> resultHandler);
	
	<ViewDataType> void query(String analyticalQueryTypeId, Period period, Parameters params, ResultHandler<ViewDataType> resultHandler);
	
}
