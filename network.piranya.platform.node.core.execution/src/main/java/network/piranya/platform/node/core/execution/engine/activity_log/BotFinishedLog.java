package network.piranya.platform.node.core.execution.engine.activity_log;

import network.piranya.platform.api.lang.Optional;

public class BotFinishedLog implements BotEsLog {

	private final String agentId;
	public String agentId() {
		return agentId;
	}
	
	private final Optional<String> errorDescription;
	public Optional<String> errorDescription() {
		return errorDescription;
	}
	
	public BotFinishedLog(String agentId, Optional<String> errorDescription) {
		this.agentId = agentId;
		this.errorDescription = errorDescription;
	}
	
}
