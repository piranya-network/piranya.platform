package network.piranya.platform.node.core.execution.sandbox;

import network.piranya.platform.api.extension_models.Parameters;
import network.piranya.platform.api.extension_models.analytics.AnalyticalView;
import network.piranya.platform.api.extension_models.analytics.AnalyticalViewDataImpl;
import network.piranya.platform.api.models.analytics.AnalyticalViewData;
import network.piranya.platform.api.models.info.MarketInfoProvider;
import network.piranya.platform.api.models.log.ActivityLog;
import network.piranya.platform.api.models.log.LpOrderAcceptedLog;

public class OrdersCounterAnalyticalView implements AnalyticalView {
	
	@Override
	public void init(Parameters params, MarketInfoProvider marketInfo) {
		data.updateData(new OrderingStats());
	}
	
	@Override
	public boolean filter(ActivityLog log) {
		return log instanceof LpOrderAcceptedLog;
	}
	
	@Override
	public void accept(ActivityLog log) {
		data.data().setOrdersCount(data.data().getOrdersCount() + 1);
		data.updateData(data.data());
	}

	@Override
	public void forget(ActivityLog log) {
	}

	@Override
	public AnalyticalViewData<?> data() {
		return data;
	}
	
	private final AnalyticalViewDataImpl<OrderingStats> data = new AnalyticalViewDataImpl<>();
	
}
