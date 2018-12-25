package network.piranya.platform.api.models.log;

import network.piranya.platform.api.models.bots.BotRef;

public class BotCreatedLog extends ActivityLog {
	
	private final BotRef botRef;
	public BotRef getBotRef() {
		return botRef;
	}
	
	private final String botTypeId;
	public String getBotTypeId() {
		return botTypeId;
	}
	
	private final String[] features;
	public String[] getFeatures() {
		return features;
	}
	
	public BotCreatedLog(BotRef botRef, String botTypeId, String[] features, long time) {
		super(time);
		this.botRef = botRef;
		this.botTypeId = botTypeId;
		this.features = features;
	}
	
}
