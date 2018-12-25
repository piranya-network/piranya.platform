package network.piranya.platform.api.models.bots;

public class BotRef {
	
	private final String botId;
	public String botId() {
		return botId;
	}
	
	public BotRef(String botId) {
		this.botId = botId;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BotRef) {
			return botId().equals(((BotRef)obj).botId());
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return botId().hashCode();
	}
	
	@Override
	public String toString() {
		return botId();
	}
	
}
