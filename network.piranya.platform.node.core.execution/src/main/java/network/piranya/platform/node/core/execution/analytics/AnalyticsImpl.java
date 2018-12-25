package network.piranya.platform.node.core.execution.analytics;

import network.piranya.platform.api.extension_models.Parameters;
import network.piranya.platform.api.lang.OpenEndedPeriod;
import network.piranya.platform.api.lang.Period;
import network.piranya.platform.api.lang.ResultHandler;
import network.piranya.platform.api.models.analytics.AnalyticalViewData;
import network.piranya.platform.api.models.analytics.Analytics;

public class AnalyticsImpl implements Analytics {
	
	@Override
	public <ViewDataType> void view(String analyticalViewId, String analyticalViewTypeId, boolean persistent, OpenEndedPeriod startPeriod, Parameters params,
			ResultHandler<AnalyticalViewData<ViewDataType>> resultHandler) {
		analyticsEngine().view(analyticalViewId, analyticalViewTypeId, persistent, startPeriod, params, resultHandler);
	}
	
	@Override
	public <ViewDataType> void query(String analyticalQueryTypeId, Period period, Parameters params, ResultHandler<ViewDataType> resultHandler) {
		analyticsEngine().query(analyticalQueryTypeId, period, params, resultHandler);
	}
	
	
	public AnalyticsImpl(AnalyticsEngine analyticsEngine) {
		this.analyticsEngine = analyticsEngine;
	}
	
	private final AnalyticsEngine analyticsEngine;
	protected AnalyticsEngine analyticsEngine() { return analyticsEngine; }
	
}
