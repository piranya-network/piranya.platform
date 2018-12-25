package network.piranya.platform.node.api.app.ui;

import network.piranya.platform.api.extension_models.app.ui.Workspace;
import network.piranya.platform.node.api.modules.ModuleMetadata;
import network.piranya.platform.node.utilities.CollectionUtils;

public class WorkspaceDetails implements Comparable<WorkspaceDetails> {
	
	private final Workspace workspace;
	public Workspace workspace() {
		return workspace;
	}
	
	private final ModuleMetadata moduleInfo;
	public ModuleMetadata moduleInfo() {
		return moduleInfo;
	}

	public WorkspaceDetails(Workspace workspace, ModuleMetadata moduleInfo) {
		this.workspace = workspace;
		this.moduleInfo = moduleInfo;
	}

	@Override
	public int compareTo(WorkspaceDetails o) {
		return CollectionUtils.difference(
				workspace().category().compareTo(o.workspace().category()),
				workspace().order() - o.workspace().order(),
				workspace().title().compareTo(o.workspace().title()),
				workspace().id().compareTo(o.workspace().id()));
	}
	
}
