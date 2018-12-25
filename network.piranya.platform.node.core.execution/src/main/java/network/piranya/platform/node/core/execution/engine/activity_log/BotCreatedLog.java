package network.piranya.platform.node.core.execution.engine.activity_log;

import network.piranya.platform.api.extension_models.Parameters;

public class BotCreatedLog implements BotEsLog {

	private final String actorClassName;
	public String actorClassName() {
		return actorClassName;
	}
	
	private final Parameters params;
	public Parameters params() {
		return params;
	}
	
	private final String botId;
	public String botId() {
		return botId;
	}
	
	public BotCreatedLog(String actorClassName, Parameters params, String botId) {
		this.actorClassName = actorClassName;
		this.params = params;
		this.botId = botId;
	}
	
}
