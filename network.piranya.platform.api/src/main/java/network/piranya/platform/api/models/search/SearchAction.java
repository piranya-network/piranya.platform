package network.piranya.platform.api.models.search;

import network.piranya.platform.api.extension_models.ActionType;

public class SearchAction {
	
	private final String label;
	public String getLabel() {
		return label;
	}
	
	private final ActionType actionType;
	public ActionType getActionType() {
		return actionType;
	}
	
	private final String actionDescriptor;
	public String getActionDescriptor() {
		return actionDescriptor;
	}
	
	public SearchAction(String label, ActionType actionType, String actionDescriptor) {
		this.label = label;
		this.actionType = actionType;
		this.actionDescriptor = actionDescriptor;
	}
	
}