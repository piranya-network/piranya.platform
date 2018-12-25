package network.piranya.platform.node.core.execution.engine.bots;

import java.util.function.Consumer;

import network.piranya.platform.api.extension_models.Parameters;
import network.piranya.platform.api.lang.ResultHandler;
import network.piranya.platform.api.models.bots.BotSpec;
import network.piranya.platform.api.models.bots.CommandBotsAdmin;
import network.piranya.platform.node.api.execution.commands.ExecutionCommand;
import network.piranya.platform.node.api.execution.commands.InvokeCommandBot;
import network.piranya.platform.node.utilities.DisposableSupport;

public class CommandBotsAdminImpl implements CommandBotsAdmin {
	
	@Override
	public void invokeCommandBot(BotSpec spec, String commandId, Parameters params, ResultHandler<Object> resultHandler) {
		disposable.checkIfDisposed();
		
		executionCommandProcessor().accept(new InvokeCommandBot(spec, commandId, params, resultHandler));
	}
	
	public void dispose() {
		disposable.markDisposed();
	}
	
	
	public CommandBotsAdminImpl(Consumer<ExecutionCommand> executionCommandProcessor) {
		this.executionCommandProcessor = executionCommandProcessor;
	}
	
	private final Consumer<ExecutionCommand> executionCommandProcessor;
	protected Consumer<ExecutionCommand> executionCommandProcessor() { return executionCommandProcessor; }
	
	private final DisposableSupport disposable = new DisposableSupport(this);
	
}
