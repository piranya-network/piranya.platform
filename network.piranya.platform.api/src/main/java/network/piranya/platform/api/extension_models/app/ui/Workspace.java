package network.piranya.platform.api.extension_models.app.ui;

public class Workspace {
	
	public static enum Category { MANUAL, AUTO, SOCIAL, SERVICES, ACCOUNT }
	
	
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
	
	private final String category;
	public String category() {
		return category;
	}
	
	private final int order;
	public int order() {
		return order;
	}
	
	public Workspace(String id, String title, String icon, String category, int order) {
		this.id = id;
		this.title = title;
		this.icon = icon;
		this.category = category;
		this.order = order;
	}
	
}
