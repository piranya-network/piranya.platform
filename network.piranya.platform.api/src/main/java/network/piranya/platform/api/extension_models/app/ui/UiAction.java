package network.piranya.platform.api.extension_models.app.ui;

public class UiAction implements Comparable<UiAction> {
	
	private final String actionId;
	public String actionId() { return actionId; }
	
	private final String title;
	public String title() { return title; }
	
	private final String launchLabel;
	public String launchLabel() { return launchLabel; }
	
	private final String description;
	public String description() { return description; }
	
	private final String[] features;
	public String[] features() { return features; }
	
	private final String[] searchTags;
	public String[] searchTags() { return searchTags; }
	
	public UiAction(String actionId, String title, String launchLabel, String description, String[] features, String[] searchTags) {
		this.actionId = actionId;
		this.title = title;
		this.launchLabel = launchLabel;
		this.description = description;
		this.features = features;
		this.searchTags = searchTags;
	}

	@Override
	public int compareTo(UiAction o) {
		return title().compareTo(o.title());
	}
	
}
