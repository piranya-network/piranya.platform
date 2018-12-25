package network.piranya.platform.node.core.execution.engine.activity_log;

import network.piranya.platform.api.extension_models.Parameters;

public class BotCommandedLog implements BotEsLog {
	
	private final String agentId;
	public String botId() {
		return agentId;
	}
	
	private final Parameters params;
	public Parameters params() {
		return params;
	}
	
	private final String commandId;
	public String commandId() {
		return commandId;
	}
	
	public BotCommandedLog(String agentId, String commandId, Parameters params) {
		this.agentId = agentId;
		this.commandId = commandId;
		this.params = params;
	}
	
}
