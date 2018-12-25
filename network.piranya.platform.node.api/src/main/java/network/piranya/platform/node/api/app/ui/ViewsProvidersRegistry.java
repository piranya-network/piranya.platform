package network.piranya.platform.node.api.app.ui;

import java.util.List;

import network.piranya.platform.api.extension_models.app.ui.ViewsProvider;
import network.piranya.platform.node.api.modules.ModuleMetadata;

public interface ViewsProvidersRegistry {
	
	<ViewsProviderType extends ViewsProvider> ViewsProvider register(Class<ViewsProviderType> viewsProviderClass, ModuleMetadata moduleInfo);
	<ViewsProviderType extends ViewsProvider> void deregister(Class<ViewsProviderType> viewsProviderClass);
	
	List<ViewsProvider> viewsProviders();
	ViewsProvider viewsProvider(String viewType);
	
}
