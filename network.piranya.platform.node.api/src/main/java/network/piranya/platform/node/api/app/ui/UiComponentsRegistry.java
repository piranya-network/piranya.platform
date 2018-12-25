package network.piranya.platform.node.api.app.ui;

import java.util.List;
import java.util.function.Consumer;

import network.piranya.platform.api.extension_models.app.ui.UiAction;
import network.piranya.platform.api.extension_models.app.ui.UiComponents;
import network.piranya.platform.node.api.modules.ModuleMetadata;

public interface UiComponentsRegistry {
	
	<UiComponentsType extends UiComponents> void register(Class<UiComponentsType> componentsClass, ModuleMetadata moduleInfo);
	<UiComponentsType extends UiComponents> void deregister(Class<UiComponentsType> componentsClass, ModuleMetadata moduleInfo);
	
	List<WorkspaceDetails> listWorkspaces();
	List<PageDetails> listPages();
	List<UiAction> listUiActions();
	
	void subscribe(Consumer<UpdateEvent> subscriber);
	void unsubscribe(Consumer<UpdateEvent> subscriber);
	
	
	public static class UpdateEvent {
		
	}
	
}
