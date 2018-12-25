package network.piranya.platform.node.core.execution;

import java.util.function.Consumer;

import network.piranya.platform.api.exceptions.PricesNotAvailableException;
import network.piranya.platform.api.extension_models.ExtensionContext;
import network.piranya.platform.api.models.info.MarketInfoProvider;
import network.piranya.platform.api.models.search.SearchService;
import network.piranya.platform.api.models.trading.ExecutionEvent;
import network.piranya.platform.api.models.trading.Instrument;
import network.piranya.platform.api.models.trading.Quote;
import network.piranya.platform.api.models.trading.TradingState;
import network.piranya.platform.node.api.booting.NetworkNodeConfig;
import network.piranya.platform.node.api.execution.ExecutionManager;
import network.piranya.platform.node.api.execution.bots.BotsRegistry;
import network.piranya.platform.node.api.execution.commands.ExecutionCommand;
import network.piranya.platform.node.api.execution.liquidity.LiquidityProvidersRegistry;
import network.piranya.platform.node.api.local_infrastructure.LocalServices;
import network.piranya.platform.node.api.local_infrastructure.LocalServicesProvider;
import network.piranya.platform.node.core.execution.engine.ExecutionEngine;

public class ExecutionManagerImpl implements ExecutionManager {
	
	@Override
	public void execute(ExecutionCommand command) {
		localExecutionEngine().execute(command);
	}
	
	@Override
	public TradingState tradingState() {
		return localExecutionEngine().tradingState();
	}
	
	@Override
	public void subscribe(Consumer<ExecutionEvent> subscriber) {
		localExecutionEngine().subscribe(subscriber);
	}
	
	@Override
	public void unsubscribe(Consumer<ExecutionEvent> subscriber) {
		localExecutionEngine().unsubscribe(subscriber);
	}
	
	@Override
	public ExtensionContext createDetachedExtensionContext(String moduleId) {
		return localExecutionEngine().createDetachedExtensionContext(moduleId);
	}
	
	@Override
	public Quote quote(Instrument instrument) throws PricesNotAvailableException {
		return localExecutionEngine().quote(instrument);
	}
	
	
	public void init() {
		localExecutionEngine().init();
	}
	
	public void dispose() {
		localExecutionEngine().dispose();
	}
	
	
	public ExecutionManagerImpl(NetworkNodeConfig nodeConfig, LocalServicesProvider localServicesProvider,
			BotsRegistry botsRegistry, LiquidityProvidersRegistry liquidityProvidersRegistry, SearchService searchService, MarketInfoProvider marketInfoProvider) {
		this.nodeConfig = nodeConfig;
		this.localServices = localServicesProvider.services(this);
		this.localExecutionEngine = new ExecutionEngine(nodeConfig, localServices(), botsRegistry, liquidityProvidersRegistry, searchService, marketInfoProvider, this);
	}
	
	private final NetworkNodeConfig nodeConfig;
	protected NetworkNodeConfig config() {
		return nodeConfig;
	}
	
	private final LocalServices localServices;
	protected LocalServices localServices() {
		return localServices;
	}
	
	private final ExecutionEngine localExecutionEngine;
	public ExecutionEngine localExecutionEngine() {
		return localExecutionEngine;
	}
	
}
