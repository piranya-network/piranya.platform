package network.piranya.platform.node.core.modules;

import static network.piranya.platform.node.utilities.CollectionUtils.*;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import network.piranya.infrastructure.dcm4j.api.ComponentContext;
import network.piranya.infrastructure.dcm4j.api.ServicesRef;
import network.piranya.platform.node.api.app.services.AppServicesRegistry;
import network.piranya.platform.node.api.app.ui.ViewsProvidersRegistry;
import network.piranya.platform.node.api.execution.bots.BotsRegistry;
import network.piranya.platform.node.api.execution.liquidity.LiquidityProvidersRegistry;
import network.piranya.platform.node.api.modules.Module;
import network.piranya.platform.node.api.modules.ModulesManager;
import network.piranya.platform.node.core.execution.ExecutionManagerImpl;
import network.piranya.platform.node.core.execution.bots.BotsRegistryImpl;
import network.piranya.platform.node.core.modules.app.AppServicesRegistryImpl;
import network.piranya.platform.node.core.modules.app.UiComponentsRegistryImpl;
import network.piranya.platform.node.core.modules.app.ViewsProvidersRegistryImpl;

public class ModulesManagerImpl implements ModulesManager {
	
	@Override
	public AppServicesRegistry appServicesRegistry() { return appServicesRegistry; }
	
	@Override
	public BotsRegistry botsRegistry() { return botsRegistry; }
	
	@Override
	public ViewsProvidersRegistry viewsProvidersRegistry() { return viewsProvidersRegistry; }
	
	protected void loadModule(Module module, network.piranya.infrastructure.dcm4j.api.ModuleInfo moduleInfo) {
		foreach(module.viewsProviders(), vp -> viewsProvidersRegistry.register(vp, module.metadata()));
		foreach(module.appServices(), serviceClass -> appServicesRegistry.register(serviceClass));
		foreach(module.uiComponents(), ui -> uiComponentsRegistry.register(ui, module.metadata()));
		foreach(module.analyticalViews(), analyticalViewType -> executionManager().localExecutionEngine().analyticsEngine().registerViewType(analyticalViewType));
		foreach(module.analyticalQueries(), analyticalQueryType -> executionManager().localExecutionEngine().analyticsEngine().registerQueryType(analyticalQueryType));
		foreach(module.liquidityProviderTypes(), lpType -> liquidityProvidersRegistry.registerLiquidityProviderType(lpType));
		foreach(module.bots(), agentType -> botsRegistry.registerBotType(agentType, module.metadata()));
	}
	
	protected void unloadModule(Module module, network.piranya.infrastructure.dcm4j.api.ModuleInfo moduleInfo) {
		// TODO
	}
	
	
	protected void onRunningModulesUpdated(ServicesRef.ServicesUpdatedEvent<Module> event) {
		foreach(event.removed(), m -> unloadModule(m.service(), m.module()));
		foreach(event.added(), m -> loadModule(m.service(), m.module()));
	}
	
	
	public void initPreExecutionInit() {
		foreach(runningModules().get(), m -> loadModule(m.service(), m.module()));
		
		runningModules().subscribe(this.runningModulesUpdateSubscriber);
	}
	
	public void initPostExecutionInit() {
		viewsProvidersRegistry.onExecutionInitialized();
		executionManager().localExecutionEngine().analyticsEngine().init();
	}
	
	public void dispose() {
		runningModules().unsubscribe(this.runningModulesUpdateSubscriber);
	}
	
	public ModulesManagerImpl(BotsRegistryImpl botsRegistry, LiquidityProvidersRegistry liquidityProvidersRegistry,
			ExecutionManagerImpl executionManager, UiComponentsRegistryImpl uiComponentsRegistry, ComponentContext componentContext, BiConsumer<String, Object> appEventsPublisher) {
		this.botsRegistry = botsRegistry;
		this.liquidityProvidersRegistry = liquidityProvidersRegistry;
		this.executionManager = executionManager;
		this.uiComponentsRegistry = uiComponentsRegistry;
		this.runningModules = componentContext.services(Module.class);
		this.appServicesRegistry = new AppServicesRegistryImpl(botsRegistry, liquidityProvidersRegistry, executionManager, appEventsPublisher);
		this.viewsProvidersRegistry = new ViewsProvidersRegistryImpl(botsRegistry, liquidityProvidersRegistry, executionManager);
	}
	
	private final ServicesRef<Module> runningModules;
	protected ServicesRef<Module> runningModules() {
		return runningModules;
	}
	
	private final Consumer<ServicesRef.ServicesUpdatedEvent<Module>> runningModulesUpdateSubscriber = this::onRunningModulesUpdated;
	
	private final AppServicesRegistryImpl appServicesRegistry;
	private final ViewsProvidersRegistryImpl viewsProvidersRegistry;
	
	private final ExecutionManagerImpl executionManager;
	protected ExecutionManagerImpl executionManager() { return executionManager; }
	
	private LiquidityProvidersRegistry liquidityProvidersRegistry;
	public LiquidityProvidersRegistry liquidityProvidersRegistry() { return liquidityProvidersRegistry; }
	
	private final UiComponentsRegistryImpl uiComponentsRegistry;
	public UiComponentsRegistryImpl uiComponentsRegistry() { return uiComponentsRegistry; }
	
	private final BotsRegistryImpl botsRegistry;
	
}
