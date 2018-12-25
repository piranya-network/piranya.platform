package network.piranya.platform.node.core.execution.engine.sandbox;

import static network.piranya.platform.node.utilities.CollectionUtils.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import network.piranya.platform.api.Version;
import network.piranya.platform.api.extension_models.Parameters;
import network.piranya.platform.api.extension_models.analytics.AnalyticalView;
import network.piranya.platform.api.extension_models.execution.bots.ExecutionBot;
import network.piranya.platform.api.extension_models.execution.liquidity.LiquidityProvider;
import network.piranya.platform.api.extension_models.execution.sandbox.SandboxConfiguration;
import network.piranya.platform.api.lang.OpenEndedPeriod;
import network.piranya.platform.api.models.trading.ExecutionEvent;
import network.piranya.platform.node.api.execution.bots.BotsRegistry;
import network.piranya.platform.node.api.execution.liquidity.LiquidityProvidersRegistry;
import network.piranya.platform.node.api.modules.ModuleMetadata;

public class SandboxConfigurer {
	
	public SandboxConfigurer(BotsRegistry currentBotsRegistry, LiquidityProvidersRegistry currentLiquidityProvidersRegistry,
			BotsRegistry botsRegistry, LiquidityProvidersRegistry liquidityProvidersRegistry) {
		this.currentBotsRegistry = currentBotsRegistry;
		this.currentLiquidityProvidersRegistry = currentLiquidityProvidersRegistry;
		this.botsRegistry = botsRegistry;
		this.liquidityProvidersRegistry = liquidityProvidersRegistry;
		
		foreach(currentLiquidityProvidersRegistry.liquidityProviderTypes(), liquidityProvidersRegistry::registerLiquidityProviderType);
		
	}
	
	private final ConfigurationContext configurationContext = new ConfigurationContext();
	public ConfigurationContext configurationContext() { return configurationContext; }
	
	private final BotsRegistry currentBotsRegistry;
	private final LiquidityProvidersRegistry currentLiquidityProvidersRegistry;
	private final BotsRegistry botsRegistry;
	private final LiquidityProvidersRegistry liquidityProvidersRegistry;
	
	private final List<BotToRun> botsToRun = new ArrayList<>();
	public List<BotToRun> botsToRun() { return botsToRun; }
	
	private final List<FinishCondition> finishConditions = new ArrayList<>();
	public List<FinishCondition> finishConditions() { return finishConditions; }
	
	private final List<AnalyticalViewParams> analyticalViews = new ArrayList<>();
	public List<AnalyticalViewParams> analyticalViews() { return analyticalViews; }
	
	
	protected class ConfigurationContext implements SandboxConfiguration.ConfigContext {
		
		@Override
		public void inheritAllLiquidityProviders() {
			foreach(currentLiquidityProvidersRegistry.liquidityProviders(), lp -> liquidityProvidersRegistry.registerLiquidityProvider(lp.getClass().getName(), lp.params()));
		}
		
		@Override
		public void inheritLiquidityProvider(String liquidityProviderId) {
			LiquidityProvider lp = currentLiquidityProvidersRegistry.liquidityProvider(liquidityProviderId);
			liquidityProvidersRegistry.registerLiquidityProvider(lp.getClass().getName(), lp.params());
		}
		
		@Override
		public <LiquidityProviderType extends LiquidityProvider> void registerLiquidityProviderType(Class<LiquidityProviderType> liquidityProviderType) {
			liquidityProvidersRegistry.registerLiquidityProviderType(liquidityProviderType);
		}
		
		@Override
		public void registerLiquidityProvider(String liquidityProviderTypeId, Parameters params) {
			liquidityProvidersRegistry.registerLiquidityProvider(liquidityProviderTypeId, params);
		}
		
		@Override
		public void inheritAllBotTypes() {
			foreach(currentBotsRegistry.botTypes(), botType -> botsRegistry.registerBotType(botType, defaultModule));
		}
		
		@Override
		public void inheritBotType(String botTypeId) {
			botsRegistry.registerBotType(currentBotsRegistry.botType(botTypeId), defaultModule);
		}
		
		@Override
		public <BotType extends ExecutionBot> void registerBotType(Class<BotType> botType) {
			botsRegistry.registerBotType(botType, defaultModule);
		}
		private final ModuleMetadata defaultModule = new ModuleMetadata("sandbox_default_module", new Version(0, 0, 0), "Sandbox Default Module", "");
		
		@Override
		public void runBot(String botTypeId, Parameters params) {
			botsToRun().add(new BotToRun(botTypeId, params, false));
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public <ViewType extends AnalyticalView> void registerAnalyticalView(String analyticalViewId, Class<ViewType> viewType, OpenEndedPeriod startPeriod, Parameters params) {
			analyticalViews().add(new AnalyticalViewParams(analyticalViewId, (Class<AnalyticalView>)viewType, startPeriod, params));
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public <Event extends ExecutionEvent> void finishCondition(Class<Event> eventType, Function<Event, Boolean> condition) {
			finishConditions().add(new FinishCondition((Class<ExecutionEvent>)eventType, (Function<ExecutionEvent, Boolean>)condition));
		}
	}
	
	public static class BotToRun {
		
		private final String botTypeId;
		public String botTypeId() {
			return botTypeId;
		}
		
		private final Parameters params;
		public Parameters params() {
			return params;
		}
		
		private final boolean isFeature;
		public boolean isFeature() { return isFeature; }
		
		public BotToRun(String botTypeId, Parameters params, boolean isFeature) {
			this.botTypeId = botTypeId;
			this.params = params;
			this.isFeature = isFeature;
		}
	}
	
	public static class FinishCondition {
		
		private final Class<ExecutionEvent> eventType;
		public Class<ExecutionEvent> eventType() {
			return eventType;
		}
		
		private final Function<ExecutionEvent, Boolean> condition;
		public Function<ExecutionEvent, Boolean> condition() {
			return condition;
		}
		
		public FinishCondition(Class<ExecutionEvent> eventType, Function<ExecutionEvent, Boolean> condition) {
			this.eventType = eventType;
			this.condition = condition;
		}
	}
	
	public static class AnalyticalViewParams {
		
		public final String analyticalViewId;
		public final Class<AnalyticalView> viewType;
		public final OpenEndedPeriod startPeriod;
		public final Parameters params;
		
		public AnalyticalViewParams(String analyticalViewId, Class<AnalyticalView> viewType,
				OpenEndedPeriod startPeriod, Parameters params) {
			this.analyticalViewId = analyticalViewId;
			this.viewType = viewType;
			this.startPeriod = startPeriod;
			this.params = params;
		}
		
	}
	
}
