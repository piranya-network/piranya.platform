package network.piranya.platform.node.api.execution.commands;

import java.util.function.Consumer;

import network.piranya.platform.api.extension_models.Data;
import network.piranya.platform.api.extension_models.Parameters;
import network.piranya.platform.api.lang.Result;
import network.piranya.platform.api.models.bots.BotRef;

public class InvokeExecutionBotCommand implements ExecutionCommand {
	
	private final BotRef botRef;
	public BotRef botRef() {
		return botRef;
	}
	
	private final String commandId;
	public String commandId() {
		return commandId;
	}
	
	private final Parameters params;
	public Parameters params() {
		return params;
	}
	
	private final Consumer<Result<Data>> resultHandler;
	public Consumer<Result<Data>> resultHandler() {
		return resultHandler;
	}
	
	public InvokeExecutionBotCommand(BotRef botRef, String commandId, Parameters params, Consumer<Result<Data>> resultHandler) {
		this.botRef = botRef;
		this.commandId = commandId;
		this.params = params;
		this.resultHandler = resultHandler;
	}
	
}
