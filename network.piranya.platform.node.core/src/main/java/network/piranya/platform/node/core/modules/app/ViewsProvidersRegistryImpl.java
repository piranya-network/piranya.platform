package network.piranya.platform.node.core.modules.app;

import static network.piranya.platform.node.utilities.ReflectionUtils.*;
import static network.piranya.platform.node.utilities.CollectionUtils.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import network.piranya.platform.api.exceptions.PiranyaException;
import network.piranya.platform.api.extension_models.app.ui.ViewsProvider;
import network.piranya.platform.node.api.app.ui.ViewsProvidersRegistry;
import network.piranya.platform.node.api.execution.ExecutionManager;
import network.piranya.platform.node.api.execution.bots.BotsRegistry;
import network.piranya.platform.node.api.execution.liquidity.LiquidityProvidersRegistry;
import network.piranya.platform.node.api.modules.ModuleMetadata;
import network.piranya.platform.node.core.execution.context.ManagerialExtensionContextImpl;

public class ViewsProvidersRegistryImpl implements ViewsProvidersRegistry {
	
	@Override
	public <ViewsProviderType extends ViewsProvider> ViewsProvider register(Class<ViewsProviderType> viewsProviderClass, ModuleMetadata moduleInfo) {
		ViewsProvider viewsProvider = createInstance(viewsProviderClass);
		inject(viewsProvider, ViewsProvider.class, "context", new ManagerialExtensionContextImpl(newContext(moduleInfo.moduleId()), liquidityProvidersRegistry));
		
		viewsProvidersMap().put(viewsProvider, true);
		
		if (isExecutionInitialized()) {
			viewsProvider.init();
		}
		
		return viewsProvider;
	}
	
	@Override
	public <ViewsProviderType extends ViewsProvider> void deregister(Class<ViewsProviderType> viewsProviderClass) {	
		find(viewsProvidersMap().keySet(), provider -> viewsProviderClass.isInstance(provider)).ifPresent(provider -> {
			getFieldValue(provider, ViewsProvider.class, "context", ManagerialExtensionContextImpl.class).dispose();
			viewsProvidersMap().remove(provider);
		});
	}
	
	@Override
	public List<ViewsProvider> viewsProviders() {
		return new ArrayList<>(viewsProvidersMap().keySet());
	}
	
	@Override
	public ViewsProvider viewsProvider(String viewType) {
		return find(viewsProvidersMap().keySet(), provider -> provider.viewType().equals(viewType)).orElseThrow(
				() -> new PiranyaException(String.format("Views Provider for view type '%s' does not exist", viewType)));
	}
	
	
	public void onExecutionInitialized() {
		this.executionInitialized = true;
		
		foreach(viewsProvidersMap().keySet(), vp -> vp.init());
	}
	
	protected boolean isExecutionInitialized() {
		return executionInitialized;
	}
	private boolean executionInitialized = false;
	
	
	protected ManagerialExtensionContextImpl newContext(String moduleId) {
		return new ManagerialExtensionContextImpl(executionManager().createDetachedExtensionContext(moduleId), liquidityProvidersRegistry());
	}
	
	
	public ViewsProvidersRegistryImpl(BotsRegistry agentsRegistry, LiquidityProvidersRegistry liquidityProvidersRegistry, ExecutionManager executionManager) {
		this.botsRegistry = agentsRegistry;
		this.liquidityProvidersRegistry = liquidityProvidersRegistry;
		this.executionManager = executionManager;
	}
	
	private final BotsRegistry botsRegistry;
	protected BotsRegistry botsRegistry() { return botsRegistry; }
	
	private final LiquidityProvidersRegistry liquidityProvidersRegistry;
	protected LiquidityProvidersRegistry liquidityProvidersRegistry() { return liquidityProvidersRegistry; }
	
	private final ExecutionManager executionManager;
	protected ExecutionManager executionManager() { return executionManager; }
	
	private final ConcurrentMap<ViewsProvider, Boolean> viewsProvidersMap = new ConcurrentHashMap<>();
	protected ConcurrentMap<ViewsProvider, Boolean> viewsProvidersMap() { return viewsProvidersMap; }
	
}
