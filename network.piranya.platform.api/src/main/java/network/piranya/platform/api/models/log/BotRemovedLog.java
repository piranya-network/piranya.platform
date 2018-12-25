package network.piranya.platform.api.models.log;

import network.piranya.platform.api.models.bots.BotRef;

public class BotRemovedLog extends ActivityLog {
	
	private final BotRef botRef;
	public BotRef getBotRef() {
		return botRef;
	}
	
	public BotRemovedLog(BotRef botRef, long time) {
		super(time);
		this.botRef = botRef;
	}
	
}
