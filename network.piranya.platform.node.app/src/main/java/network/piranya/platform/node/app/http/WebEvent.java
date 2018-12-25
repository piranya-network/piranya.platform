package network.piranya.platform.node.app.http;

public class WebEvent {
	
	public WebEvent(String eventType, Object data) {
		this.eventType = eventType;
		this.data = data;
	}
	
	private final String eventType;
	public String getEventType() {
		return eventType;
	}
	
	private final Object data;
	public Object getData() {
		return data;
	}
	
}
