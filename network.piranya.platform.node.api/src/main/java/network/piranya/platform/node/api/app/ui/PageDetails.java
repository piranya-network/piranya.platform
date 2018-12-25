package network.piranya.platform.node.api.app.ui;

import network.piranya.platform.api.extension_models.app.ui.Page;
import network.piranya.platform.node.api.modules.ModuleMetadata;

public class PageDetails {
	
	private final Page page;
	public Page page() {
		return page;
	}
	
	private final ModuleMetadata moduleInfo;
	public ModuleMetadata moduleInfo() {
		return moduleInfo;
	}

	public PageDetails(Page page, ModuleMetadata moduleInfo) {
		this.page = page;
		this.moduleInfo = moduleInfo;
	}
	
}
