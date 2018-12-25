package network.piranya.platform.node.api.execution.commands;

import network.piranya.platform.api.extension_models.Parameters;
import network.piranya.platform.api.lang.ResultHandler;
import network.piranya.platform.api.models.bots.BotSpec;

public class InvokeCommandBot implements ExecutionCommand {
	
	private final BotSpec spec;
	public BotSpec spec() { return spec; }
	
	private final String commandId;
	public String commandId() { return commandId; }
	
	private final Parameters params;
	public Parameters params() { return params; }
	
	private final ResultHandler<Object> resultHandler;
	public ResultHandler<Object> resultHandler() { return resultHandler; }
	
	public InvokeCommandBot(BotSpec spec, String commandId, Parameters params, ResultHandler<Object> resultHandler) {
		this.spec = spec;
		this.commandId = commandId;
		this.params = params;
		this.resultHandler = resultHandler;
	}
	
}
