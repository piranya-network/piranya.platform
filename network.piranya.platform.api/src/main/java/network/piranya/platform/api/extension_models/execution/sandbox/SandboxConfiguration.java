package network.piranya.platform.api.extension_models.execution.sandbox;

import java.util.function.Function;

import network.piranya.platform.api.extension_models.Parameters;
import network.piranya.platform.api.extension_models.analytics.AnalyticalView;
import network.piranya.platform.api.extension_models.execution.bots.ExecutionBot;
import network.piranya.platform.api.extension_models.execution.liquidity.LiquidityProvider;
import network.piranya.platform.api.lang.OpenEndedPeriod;
import network.piranya.platform.api.models.trading.ExecutionEvent;
import network.piranya.platform.api.utilities.Utilities;

public abstract class SandboxConfiguration {
	
	public abstract void configure(ConfigContext context);
	
	
	protected Utilities utils() { return utils; }
	protected Utilities utils = new Utilities();
	
	
	public static interface ConfigContext {
		
		void inheritAllLiquidityProviders();
		void inheritLiquidityProvider(String liquidityProviderId);
		<LiquidityProviderType extends LiquidityProvider> void registerLiquidityProviderType(Class<LiquidityProviderType> liquidityProviderType);
		void registerLiquidityProvider(String liquidityProviderTypeId, Parameters params);
		
		void inheritAllBotTypes();
		void inheritBotType(String botTypeId);
		<BotType extends ExecutionBot> void registerBotType(Class<BotType> botType);
		void runBot(String botTypeId, Parameters build);
		
		<ViewType extends AnalyticalView> void registerAnalyticalView(String analyticalViewId, Class<ViewType> viewType, OpenEndedPeriod startPeriod, Parameters params);
		
		<Event extends ExecutionEvent> void finishCondition(Class<Event> eventType, Function<Event, Boolean> condition);
	}
	
	public static interface StartContext {
		
		void startBot(String botTypeId, Parameters build);
	}
	
}
