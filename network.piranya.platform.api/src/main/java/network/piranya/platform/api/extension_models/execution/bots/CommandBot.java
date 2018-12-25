package network.piranya.platform.api.extension_models.execution.bots;

import network.piranya.platform.api.extension_models.ManagerialExtensionContext;
import network.piranya.platform.api.utilities.Utilities;

public class CommandBot extends Bot {
	
	protected ManagerialExtensionContext context() {
		return this.context;
	}
	private ManagerialExtensionContext context;
	
	protected final Utilities utils = new Utilities();
	protected Utilities utils() { return utils; }
	
}
