package network.piranya.platform.node.api.modules;

import network.piranya.platform.node.api.app.services.AppServicesRegistry;
import network.piranya.platform.node.api.app.ui.ViewsProvidersRegistry;
import network.piranya.platform.node.api.execution.bots.BotsRegistry;

public interface ModulesManager {
	
	AppServicesRegistry appServicesRegistry();
	
	BotsRegistry botsRegistry();
	
	ViewsProvidersRegistry viewsProvidersRegistry();
	
}
