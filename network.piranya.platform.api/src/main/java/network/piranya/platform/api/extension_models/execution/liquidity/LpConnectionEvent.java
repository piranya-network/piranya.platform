package network.piranya.platform.api.extension_models.execution.liquidity;

public class LpConnectionEvent implements LpEvent {
	
	public static enum EventType { CONNECTED, DISCONNECTED }
	
	public LpConnectionEvent(String lpId, EventType eventType) {
		this.lpId = lpId;
		this.eventType = eventType;
	}
	
	private final String lpId;
	public String lpId() { return lpId; }
	
	private final EventType eventType;
	public EventType eventType() { return eventType; }
	
}
