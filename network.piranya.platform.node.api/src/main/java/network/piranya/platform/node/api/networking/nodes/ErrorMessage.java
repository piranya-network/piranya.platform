package network.piranya.platform.node.api.networking.nodes;

public class ErrorMessage extends Message {
	
	private final String errorType;
	public String errorType() {
		return errorType;
	}
	
	private final String title;
	public String title() {
		return title;
	}
	
	private final String description;
	public String description() {
		return description;
	}
	
	public ErrorMessage(String errorType, String title, String description) {
		this.errorType = errorType;
		this.title = title;
		this.description = description;
	}
	
}
