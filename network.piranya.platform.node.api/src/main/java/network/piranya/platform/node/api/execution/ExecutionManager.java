package network.piranya.platform.node.api.execution;

import java.util.function.Consumer;

import network.piranya.platform.api.exceptions.PricesNotAvailableException;
import network.piranya.platform.api.extension_models.ExtensionContext;
import network.piranya.platform.api.models.trading.ExecutionEvent;
import network.piranya.platform.api.models.trading.Instrument;
import network.piranya.platform.api.models.trading.Quote;
import network.piranya.platform.api.models.trading.TradingState;
import network.piranya.platform.node.api.execution.commands.ExecutionCommand;

public interface ExecutionManager {
	
	void execute(ExecutionCommand command);
	
	TradingState tradingState();
	Quote quote(Instrument instrument) throws PricesNotAvailableException;
	
	void subscribe(Consumer<ExecutionEvent> subscriber);
	void unsubscribe(Consumer<ExecutionEvent> subscriber);
	
	ExtensionContext createDetachedExtensionContext(String moduleId);
	
}
