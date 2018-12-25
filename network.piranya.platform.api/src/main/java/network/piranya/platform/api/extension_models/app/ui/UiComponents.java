package network.piranya.platform.api.extension_models.app.ui;

public abstract class UiComponents {
	
	public Workspace[] workspaces() {
		return new Workspace[0];
	}
	
	public Page[] pages() {
		return new Page[0];
	}
	
	public UiAction[] uiActions() {
		return new UiAction[0];
	}
	
}
