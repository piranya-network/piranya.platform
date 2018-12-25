package network.piranya.platform.api.models.metadata;

public class CommandInfo {
	
	private final String commandId;
	public String getCommandId() {
		return commandId;
	}
	
	private final String displayName;
	public String getDisplayName() {
		return displayName;
	}
	
	private final String description;
	public String getDescription() {
		return description;
	}
	
	private final InputInfo[] inputs;
	public InputInfo[] getInputs() {
		return inputs;
	}
	
	private final String[] features;
	public String[] getFeatures() {
		return features;
	}
	
	private final SearchInfo searchInfo;
	public SearchInfo getSearchInfo() {
		return searchInfo;
	}
	
	private final int order;
	public int getOrder() {
		return order;
	}
	
	public CommandInfo(String commandId, String displayName, String description, InputInfo[] inputs, String[] features, SearchInfo searchInfo, int order) {
		this.commandId = commandId;
		this.displayName = displayName;
		this.description = description;
		this.inputs = inputs;
		this.features = features;
		this.searchInfo = searchInfo;
		this.order = order;
	}
	
}
