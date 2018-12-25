package network.piranya.platform.node.core.execution.testing.support;

import java.io.File;

import network.piranya.platform.api.Version;
import network.piranya.platform.api.accounting.NodeId;
import network.piranya.platform.api.extension_models.ParametersBuilder;
import network.piranya.platform.api.extension_models.execution.bots.Bot;
import network.piranya.platform.api.extension_models.execution.liquidity.LiquidityProvider;
import network.piranya.platform.node.api.booting.NetworkNodeConfig;
import network.piranya.platform.node.api.execution.bots.BotsRegistry;
import network.piranya.platform.node.api.modules.ModuleMetadata;
import network.piranya.platform.node.core.execution.bots.BotsRegistryImpl;
import network.piranya.platform.node.core.execution.engine.ExecutionEngine;
import network.piranya.platform.node.core.execution.liquidity.LiquidityProvidersRegistryImpl;
import network.piranya.platform.node.core.execution.testing.utils.FileUtils;
import network.piranya.platform.node.core.local_infrastructure.LocalServicesImpl;
import network.piranya.platform.node.core.local_infrastructure.storage.LocalStorageImpl;

public class EngineSupportWithLp<Lp extends LiquidityProvider> {
	
	public BotsRegistry botsRegistry;
	public LiquidityProvidersRegistryImpl liquidityProvidersRegistry;
	public Lp lp;
	public LocalServicesImpl localServices;
	public LocalStorageImpl localStorage;
	public ExecutionEngine executionEngine;
	
	@SafeVarargs
	public EngineSupportWithLp(File dir, Class<Lp> lpClass, Class<? extends Bot>... agentsTypes) {
		this(dir, lpClass, true, agentsTypes);
	}
	
	@SuppressWarnings("unchecked")
	@SafeVarargs
	public EngineSupportWithLp(File dir, Class<Lp> lpClass, boolean registerLp, Class<? extends Bot>... agentsTypes) {
		NetworkNodeConfig config = new NetworkNodeConfig(new NodeId("node", 0), 8000, FileUtils.deleteOnExit(dir));
		localStorage = new LocalStorageImpl(config);
		
		botsRegistry = new BotsRegistryImpl(localStorage);
		for (Class<? extends Bot> agentType : agentsTypes) {
			botsRegistry.registerBotType(agentType, new ModuleMetadata("test", new Version(1, 0, 0), "Test Module", ""));
		}
		
		liquidityProvidersRegistry = new LiquidityProvidersRegistryImpl(() -> executionEngine, (instrument, lpId) -> {}, null);
		
		localServices = new LocalServicesImpl(this, config, null, localStorage);
		
		TestsExecutionManager executionManager = new TestsExecutionManager();
		executionEngine = new ExecutionEngine(config, this.localServices, this.botsRegistry, this.liquidityProvidersRegistry, null, null, executionManager);
		executionManager.setExecutionEngine(executionEngine);
		
		if (registerLp) {
			liquidityProvidersRegistry.registerLiquidityProviderType(lpClass);
			lp = (Lp)liquidityProvidersRegistry.registerLiquidityProvider(lpClass.getName(), new ParametersBuilder().string("core:lp_id", "LP0").build());
			lp.connect(result -> {});
		}
	}
	
	public void dispose() {
		executionEngine.dispose();
		localServices.dispose();
		localStorage.dispose();
	}
}