package network.piranya.platform.node.app.osgi;

import java.util.List;

import network.piranya.platform.api.Version;
import network.piranya.platform.api.extension_models.app.AppService;
import network.piranya.platform.node.api.modules.Module;
import network.piranya.platform.node.api.modules.ModuleMetadata;
import network.piranya.platform.node.app.services.CommandingAppService;
import network.piranya.platform.node.app.services.SearchAppService;
import network.piranya.platform.node.app.services.UserPrefsAppService;

public class ModuleImpl extends Module {
	
	@Override
	public ModuleMetadata metadata() {
		return new ModuleMetadata("network.piranya.platform.node.app", new Version(1, 0, 0), "Piranaya App Module", "");
	}

	@Override
	public List<Class<? extends AppService>> appServices() {
		return list(SearchAppService.class, CommandingAppService.class, UserPrefsAppService.class);
	}
	
}
