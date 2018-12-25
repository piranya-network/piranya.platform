package network.piranya.platform.api.extension_models.app.ui;

public class View {
	
	public View(String viewType, String id, Object data) {
		this.viewType = viewType;
		this.id = id;
		this.data = data;
	}
	
	private final String viewType;
	public String getViewType() {
		return viewType;
	}
	
	private final String id;
	public String getId() {
		return id;
	}
	
	private final Object data;
	public Object getData() {
		return data;
	}
	
}
