package network.piranya.platform.node.api.modules;

import java.util.Arrays;
import java.util.List;

import network.piranya.platform.api.extension_models.analytics.AnalyticalQuery;
import network.piranya.platform.api.extension_models.analytics.AnalyticalView;
import network.piranya.platform.api.extension_models.app.AppService;
import network.piranya.platform.api.extension_models.app.ui.UiComponents;
import network.piranya.platform.api.extension_models.app.ui.ViewsProvider;
import network.piranya.platform.api.extension_models.execution.bots.Bot;
import network.piranya.platform.api.extension_models.execution.liquidity.LiquidityProvider;

public abstract class Module {
	
	public abstract ModuleMetadata metadata();
	
	
	public List<Class<? extends Bot>> bots() {
		return list();
	}
	
	public List<Class<? extends LiquidityProvider>> liquidityProviderTypes() {
		return list();
	}
	
	public List<Class<? extends AnalyticalView>> analyticalViews() {
		return list();
	}
	
	public List<Class<? extends AnalyticalQuery>> analyticalQueries() {
		return list();
	}
	
	public List<Class<? extends AppService>> appServices() {
		return list();
	}
	
	public List<Class<? extends ViewsProvider>> viewsProviders() {
		return list();
	}
	
	public List<Class<? extends UiComponents>> uiComponents() {
		return list();
	}
	
	
	@SafeVarargs
	protected final <T> List<T> list(T... items) {
		return Arrays.asList(items);
	}
	
}
