package network.piranya.platform.api.models.execution.sandbox;

import java.util.function.Consumer;

import network.piranya.platform.api.models.analytics.AnalyticalViewData;
import network.piranya.platform.api.models.bots.BotsListing;

public interface Sandbox {
	
	void runAsync();
	void abort();
	
	BotsListing bots();
	
	<ViewDataType> AnalyticalViewData<ViewDataType> getAnalyticalView(String viewId);
	
	void onFinish(Runnable listener);
	void onError(Consumer<Exception> listener);
	
}
