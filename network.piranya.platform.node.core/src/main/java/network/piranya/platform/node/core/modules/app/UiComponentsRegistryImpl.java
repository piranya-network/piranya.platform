package network.piranya.platform.node.core.modules.app;

import static network.piranya.platform.node.utilities.ReflectionUtils.createInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import network.piranya.platform.api.extension_models.app.ui.Page;
import network.piranya.platform.api.extension_models.app.ui.UiAction;
import network.piranya.platform.api.extension_models.app.ui.UiComponents;
import network.piranya.platform.api.extension_models.app.ui.Workspace;
import network.piranya.platform.node.api.app.ui.PageDetails;
import network.piranya.platform.node.api.app.ui.UiComponentsRegistry;
import network.piranya.platform.node.api.app.ui.WorkspaceDetails;
import network.piranya.platform.node.api.modules.ModuleMetadata;
import network.piranya.platform.node.utilities.EventsSubscriptionSupport;

public class UiComponentsRegistryImpl implements UiComponentsRegistry {
	
	@Override
	public <UiComponentsType extends UiComponents> void register(Class<UiComponentsType> componentsClass, ModuleMetadata moduleInfo) {
		UiComponentsInfo info = new UiComponentsInfo(createInstance(componentsClass), moduleInfo);
		UiComponentsInfo current = uiComponentsMap().get(info.key());
		if (current == null || info.moduleInfo().version().compareTo(current.moduleInfo().version()) > 0) {
			uiComponentsMap().put(info.key(), info);
			subscriptionsSupport().publish(new UpdateEvent(), true);
		}
	}
	
	@Override
	public <UiComponentsType extends UiComponents> void deregister(Class<UiComponentsType> componentsClass, ModuleMetadata moduleInfo) {
		UiComponentsInfo info = new UiComponentsInfo(createInstance(componentsClass), moduleInfo);
		uiComponentsMap().remove(info.key());
		subscriptionsSupport().publish(new UpdateEvent(), true);
	}
	
	@Override
	public List<WorkspaceDetails> listWorkspaces() {
		SortedSet<WorkspaceDetails> result = new TreeSet<>();
		for (UiComponentsInfo components : uiComponentsMap().values()) {
			for (Workspace workspace : components.uiComponents().workspaces()) {
				result.add(new WorkspaceDetails(workspace, components.moduleInfo()));
			}
		}
		return new ArrayList<>(result);
	}
	
	@Override
	public List<PageDetails> listPages() {
		SortedSet<PageDetails> result = new TreeSet<>();
		for (UiComponentsInfo components : uiComponentsMap().values()) {
			for (Page page : components.uiComponents().pages()) {
				result.add(new PageDetails(page, components.moduleInfo()));
			}
		}
		return new ArrayList<>(result);
	}
	
	@Override
	public List<UiAction> listUiActions() {
		SortedSet<UiAction> result = new TreeSet<>();
		for (UiComponentsInfo components : uiComponentsMap().values()) {
			for (UiAction action : components.uiComponents().uiActions()) {
				result.add(action);
			}
		}
		return new ArrayList<>(result);
	}
	
	@Override
	public void subscribe(Consumer<UpdateEvent> subscriber) {
		subscriptionsSupport().subscribe(subscriber);
	}
	
	@Override
	public void unsubscribe(Consumer<UpdateEvent> subscriber) {
		subscriptionsSupport().unsubscribe(subscriber);
	}
	
	
	private final ConcurrentMap<String, UiComponentsInfo> uiComponentsMap = new ConcurrentHashMap<>();
	protected ConcurrentMap<String, UiComponentsInfo> uiComponentsMap() {
		return uiComponentsMap;
	}
	
	private final EventsSubscriptionSupport<UpdateEvent> subscriptionsSupport = new EventsSubscriptionSupport<>();
	protected EventsSubscriptionSupport<UpdateEvent> subscriptionsSupport() {
		return subscriptionsSupport;
	}
	
	
	protected class UiComponentsInfo {
		
		public UiComponentsInfo(UiComponents uiComponents, ModuleMetadata moduleInfo) {
			this.uiComponents = uiComponents;
			this.moduleInfo = moduleInfo;
			this.key = String.format("%s:%s", uiComponents.getClass().getName(), moduleInfo.moduleId());
		}
		
		private final UiComponents uiComponents;
		public UiComponents uiComponents() {
			return uiComponents;
		}
		
		private final ModuleMetadata moduleInfo;
		public ModuleMetadata moduleInfo() {
			return moduleInfo;
		}
		
		private final String key;
		public String key() {
			return key;
		}
	}
	
}
