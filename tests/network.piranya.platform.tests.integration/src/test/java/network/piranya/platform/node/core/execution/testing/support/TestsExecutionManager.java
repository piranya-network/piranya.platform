package network.piranya.platform.node.core.execution.testing.support;

import java.util.function.Consumer;

import network.piranya.platform.api.exceptions.PricesNotAvailableException;
import network.piranya.platform.api.extension_models.ExtensionContext;
import network.piranya.platform.api.models.trading.ExecutionEvent;
import network.piranya.platform.api.models.trading.Instrument;
import network.piranya.platform.api.models.trading.Quote;
import network.piranya.platform.api.models.trading.TradingState;
import network.piranya.platform.node.api.execution.ExecutionManager;
import network.piranya.platform.node.api.execution.commands.ExecutionCommand;
import network.piranya.platform.node.core.execution.engine.ExecutionEngine;

public class TestsExecutionManager implements ExecutionManager {
	
	@Override
	public void execute(ExecutionCommand command) {
		executionEngine.execute(command);
	}
	
	@Override
	public TradingState tradingState() {
		return executionEngine.tradingState();
	}
	
	@Override
	public ExtensionContext createDetachedExtensionContext(String moduleId) {
		return executionEngine.createDetachedExtensionContext(moduleId);
	}
	
	@Override
	public void subscribe(Consumer<ExecutionEvent> subscriber) {
		executionEngine.subscribe(subscriber);
	}
	
	@Override
	public void unsubscribe(Consumer<ExecutionEvent> subscriber) {
		executionEngine.unsubscribe(subscriber);
	}
	
	@Override
	public Quote quote(Instrument instrument) throws PricesNotAvailableException {
		return executionEngine.quote(instrument);
	}
	
	public void setExecutionEngine(ExecutionEngine executionEngine) {
		this.executionEngine = executionEngine;
	}
	
	private ExecutionEngine executionEngine;
	
}