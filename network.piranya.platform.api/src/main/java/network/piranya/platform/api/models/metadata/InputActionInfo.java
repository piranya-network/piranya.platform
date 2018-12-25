package network.piranya.platform.api.models.metadata;

public class InputActionInfo {
	
	private final String actionDescriptor;
	public String getActionDescriptor() {
		return actionDescriptor;
	}
	
	private final String label;
	public String getLabel() {
		return label;
	}
	
	public InputActionInfo(String actionDescriptor, String label) {
		this.actionDescriptor = actionDescriptor;
		this.label = label;
	}
	
}
