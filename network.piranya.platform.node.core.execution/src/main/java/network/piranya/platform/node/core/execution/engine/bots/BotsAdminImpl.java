package network.piranya.platform.node.core.execution.engine.bots;

import java.util.List;
import java.util.function.Consumer;

import network.piranya.platform.api.models.bots.BotsAdmin;
import network.piranya.platform.api.models.bots.CommandBotsAdmin;
import network.piranya.platform.api.models.bots.ExecutionBotsAdmin;
import network.piranya.platform.api.models.metadata.BotTypeInfo;
import network.piranya.platform.node.api.execution.bots.BotsRegistry;
import network.piranya.platform.node.api.execution.commands.ExecutionCommand;
import network.piranya.platform.node.utilities.DisposableSupport;

public class BotsAdminImpl implements BotsAdmin {
	
	@Override
	public ExecutionBotsAdmin execution() {
		disposable.checkIfDisposed();
		return executionBotsAdmin;
	}
	
	@Override
	public CommandBotsAdmin commands() {
		disposable.checkIfDisposed();
		return commandBotsAdmin;
	}
	
	@Override
	public BotTypeInfo botMetadata(String botTypeId) {
		disposable.checkIfDisposed();
		
		return execution().botMetadata(botTypeId);
	}
	
	@Override
	public List<BotTypeInfo> botTypes() {
		disposable.checkIfDisposed();
		
		return execution().botTypes();
	}
	
	public void dispose() {
		disposable.markDisposed();
		executionBotsAdmin.dispose();
		commandBotsAdmin.dispose();
	}
	
	
	public BotsAdminImpl(Consumer<ExecutionCommand> executionCommandProcessor, BotsRegistry registry) {
		this.executionBotsAdmin = new ExecutionBotsAdminImpl(executionCommandProcessor, registry);
		this.commandBotsAdmin = new CommandBotsAdminImpl(executionCommandProcessor);
	}
	
	private final ExecutionBotsAdminImpl executionBotsAdmin;
	private final CommandBotsAdminImpl commandBotsAdmin;
	private final DisposableSupport disposable = new DisposableSupport(this);
	
}
