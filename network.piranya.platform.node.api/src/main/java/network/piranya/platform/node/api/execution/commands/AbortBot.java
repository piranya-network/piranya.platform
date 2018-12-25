package network.piranya.platform.node.api.execution.commands;

import java.util.function.Consumer;

import network.piranya.platform.api.lang.Result;
import network.piranya.platform.api.models.bots.BotRef;
import network.piranya.platform.api.models.bots.BotView;

public class AbortBot implements ExecutionCommand {
	
	private final BotRef botRef;
	public BotRef botRef() {
		return botRef;
	}
	
	private final Consumer<Result<BotView>> resultHandler;
	public Consumer<Result<BotView>> resultHandler() {
		return resultHandler;
	}
	
	public AbortBot(BotRef botRef, Consumer<Result<BotView>> resultHandler) {
		this.botRef = botRef;
		this.resultHandler = resultHandler;
	}
	
}
