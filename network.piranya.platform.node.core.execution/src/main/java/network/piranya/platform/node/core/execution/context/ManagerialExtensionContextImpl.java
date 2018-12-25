package network.piranya.platform.node.core.execution.context;

import network.piranya.platform.api.extension_models.ExtensionContext;
import network.piranya.platform.api.extension_models.ManagerialExtensionContext;
import network.piranya.platform.api.extension_models.execution.sandbox.SandboxConfiguration;
import network.piranya.platform.api.models.analytics.Analytics;
import network.piranya.platform.api.models.bots.BotsAdmin;
import network.piranya.platform.api.models.execution.ExecutionEngineReader;
import network.piranya.platform.api.models.execution.sandbox.Sandbox;
import network.piranya.platform.api.models.info.MarketInfoProvider;
import network.piranya.platform.api.models.infrastructure.Executor;
import network.piranya.platform.api.models.infrastructure.SerializationServices;
import network.piranya.platform.api.models.infrastructure.net.NetServices;
import network.piranya.platform.api.models.infrastructure.storage.StorageServices;
import network.piranya.platform.api.models.log.ActivityLogStore;
import network.piranya.platform.api.models.search.SearchService;
import network.piranya.platform.api.models.trading.liquidity.LiquidityProvidersAdmin;
import network.piranya.platform.node.api.execution.liquidity.LiquidityProvidersRegistry;

public class ManagerialExtensionContextImpl implements ManagerialExtensionContext {
	
	@Override
	public BotsAdmin bots() {
		return extensionContext().bots();
	}
	
	@Override
	public ExecutionEngineReader executionEngine() {
		return extensionContext().executionEngine();
	}
	
	@Override
	public Analytics analytics() {
		return extensionContext().analytics();
	}
	
	@Override
	public Executor executor() {
		return extensionContext().executor();
	}
	
	@Override
	public StorageServices storage() {
		return extensionContext().storage();
	}
	
	@Override
	public NetServices net() {
		return extensionContext().net();
	}
	
	@Override
	public SerializationServices serialization() {
		return extensionContext().serialization();
	}
	
	@Override
	public ActivityLogStore activityLog() {
		return extensionContext().activityLog();
	}
	
	@Override
	public MarketInfoProvider marketInfo() {
		return extensionContext().marketInfo();
	}
	
	@Override
	public <SandboxConfigurationClass extends SandboxConfiguration> Sandbox createSandbox(SandboxConfigurationClass sandboxConfig) {
		return extensionContext().createSandbox(sandboxConfig);
	}
	
	@Override
	public SearchService search() {
		return extensionContext().search();
	}
	
	@Override
	public LiquidityProvidersAdmin liquidityProvidersAdmin() {
		return liquidityProvidersAdmin;
	}
	
	public void dispose() {
		//extensionContext().dispose
		liquidityProvidersAdmin.dispose();
	}
	
	
	public ManagerialExtensionContextImpl(ExtensionContext extensionContext, LiquidityProvidersRegistry liquidityProvidersRegistry) {
		this.extensionContext = extensionContext;
		this.liquidityProvidersAdmin = new LiquidityProvidersAdminImpl(liquidityProvidersRegistry);
	}
	
	private final ExtensionContext extensionContext;
	protected ExtensionContext extensionContext() { return extensionContext; }
	
	private final LiquidityProvidersAdminImpl liquidityProvidersAdmin;
	
}
