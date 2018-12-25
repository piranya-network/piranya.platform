package network.piranya.platform.node.core.execution.engine.bots;

import java.util.function.Consumer;

import network.piranya.platform.api.extension_models.Data;
import network.piranya.platform.api.extension_models.Parameters;
import network.piranya.platform.api.extension_models.execution.bots.BotEvent;
import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.api.lang.Result;
import network.piranya.platform.api.models.bots.BotRef;
import network.piranya.platform.api.models.bots.BotSpec;
import network.piranya.platform.api.models.bots.BotView;
import network.piranya.platform.api.models.bots.ExecutionBotsAdmin;
import network.piranya.platform.node.api.execution.bots.BotsRegistry;
import network.piranya.platform.node.api.execution.commands.AbortBot;
import network.piranya.platform.node.api.execution.commands.CreateBot;
import network.piranya.platform.node.api.execution.commands.ExecutionCommand;
import network.piranya.platform.node.api.execution.commands.InvokeExecutionBotCommand;
import network.piranya.platform.node.utilities.DisposableSupport;

public class ExecutionBotsAdminImpl extends BotsListingImpl implements ExecutionBotsAdmin {
	
	@Override
	public void startBot(BotSpec spec, Parameters params, Consumer<Result<BotView>> resultHandler, Consumer<BotEvent> eventsSubscriber) {
		disposable.checkIfDisposed();
		executionCommandProcessor().accept(CreateBot.create(spec, params, Optional.of(eventsSubscriber), resultHandler));
	}
	
	@Override
	public void startBot(BotSpec spec, Parameters params, Consumer<Result<BotView>> resultHandler) {
		disposable.checkIfDisposed();
		executionCommandProcessor().accept(CreateBot.create(spec, params, Optional.empty(), resultHandler));
	}
	
	@Override
	public void invokeBot(BotRef botRef, String commandId, Parameters params, Consumer<Result<Data>> resultHandler) {
		disposable.checkIfDisposed();
		executionCommandProcessor().accept(new InvokeExecutionBotCommand(botRef, commandId, params, resultHandler));
	}
	
	@Override
	public void abortBot(BotRef botRef, Consumer<Result<BotView>> resultHandler) {
		disposable.checkIfDisposed();
		executionCommandProcessor().accept(new AbortBot(botRef, resultHandler));
	}
	
	public void dispose() {
		disposable.markDisposed();
	}
	
	
	public ExecutionBotsAdminImpl(Consumer<ExecutionCommand> executionCommandProcessor, BotsRegistry registry) {
		super(registry);
		this.executionCommandProcessor = executionCommandProcessor;
	}
	
	
	private final Consumer<ExecutionCommand> executionCommandProcessor;
	protected Consumer<ExecutionCommand> executionCommandProcessor() { return executionCommandProcessor; }
	
	private final DisposableSupport disposable = new DisposableSupport(this);
	
}
