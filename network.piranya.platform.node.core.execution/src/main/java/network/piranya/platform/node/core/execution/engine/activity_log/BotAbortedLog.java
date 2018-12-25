package network.piranya.platform.node.core.execution.engine.activity_log;

public class BotAbortedLog implements BotEsLog {

	private final String agentId;
	public String botId() {
		return agentId;
	}
	
	public BotAbortedLog(String agentId) {
		this.agentId = agentId;
	}
	
}
