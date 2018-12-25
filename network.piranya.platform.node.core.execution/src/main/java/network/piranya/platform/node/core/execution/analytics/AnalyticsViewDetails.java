package network.piranya.platform.node.core.execution.analytics;

import network.piranya.platform.api.extension_models.Parameters;
import network.piranya.platform.api.lang.OpenEndedPeriod;

public class AnalyticsViewDetails {
	
	private final String viewId;
	public String viewId() { return viewId; }
	
	private final String viewTypeId;
	public String viewTypeId() { return viewTypeId; }
	
	private final OpenEndedPeriod startPeriod;
	public OpenEndedPeriod startPeriod() { return startPeriod; }
	
	private final Parameters params;
	public Parameters params() { return params; }
	
	public AnalyticsViewDetails(String viewId, String viewTypeId, OpenEndedPeriod startPeriod, Parameters params) {
		this.viewId = viewId;
		this.viewTypeId = viewTypeId;
		this.startPeriod = startPeriod;
		this.params = params;
	}
	
}
