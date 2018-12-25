package network.piranya.platform.node.core.execution.analytics;

import java.math.BigDecimal;

import network.piranya.platform.api.extension_models.Parameters;
import network.piranya.platform.api.extension_models.analytics.AnalyticalQuery;
import network.piranya.platform.api.extension_models.analytics.AnalyticalView;
import network.piranya.platform.api.extension_models.analytics.AnalyticalViewDataImpl;
import network.piranya.platform.api.models.analytics.AnalyticalViewData;
import network.piranya.platform.api.models.info.MarketInfoProvider;
import network.piranya.platform.api.models.log.ActivityLog;
import network.piranya.platform.api.models.log.LpFillLog;

public class FillsStatsAnalyticalView implements AnalyticalView, AnalyticalQuery {
	
	@Override
	public void init(Parameters params, MarketInfoProvider marketInfo) {
		data.updateData(new FillsStats(fillsCount, totalFillsSize));
	}
	
	@Override
	public boolean filter(ActivityLog log) {
		return log instanceof LpFillLog;
	}
	
	@Override
	public void accept(ActivityLog log) {
		LpFillLog f = (LpFillLog)log;
		fillsCount++;
		totalFillsSize = totalFillsSize.add(f.getFill().size().abs());
		
		data.updateData(new FillsStats(fillsCount, totalFillsSize));
	}

	@Override
	public void forget(ActivityLog log) {
		LpFillLog f = (LpFillLog)log;
		fillsCount++;
		totalFillsSize = totalFillsSize.subtract(f.getFill().size().abs());
		
		data.updateData(new FillsStats(fillsCount, totalFillsSize));
	}
	
	@Override
	public AnalyticalViewData<?> data() {
		return data;
	}
	private final AnalyticalViewDataImpl<FillsStats> data = new AnalyticalViewDataImpl<>();
	
	private int fillsCount = 0;
	private BigDecimal totalFillsSize = new BigDecimal("0");
	
	@Override
	public Object getQueryResult() {
		return data.data();
	}
	
}
