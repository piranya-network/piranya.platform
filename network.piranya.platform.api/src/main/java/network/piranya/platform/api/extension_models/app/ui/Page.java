package network.piranya.platform.api.extension_models.app.ui;

import network.piranya.platform.api.lang.Optional;

public class Page {
	
	private final String id;
	public String id() {
		return id;
	}
	
	private final String title;
	public String title() {
		return title;
	}
	
	private final String icon;
	public String icon() {
		return icon;
	}
	
	private final Optional<String> workspaceId;
	public Optional<String> workspaceId() {
		return workspaceId;
	}
	
	public Page(String id, String title, String icon, Optional<String> workspaceId) {
		this.id = id;
		this.title = title;
		this.icon = icon;
		this.workspaceId = workspaceId;
	}
	
}
