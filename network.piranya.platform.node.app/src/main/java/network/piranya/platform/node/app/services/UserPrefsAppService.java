package network.piranya.platform.node.app.services;

import java.util.List;
import java.util.Map;

import network.piranya.platform.api.extension_models.Parameters;
import network.piranya.platform.api.extension_models.app.AppService;
import network.piranya.platform.api.extension_models.app.AppServiceMetadata;
import network.piranya.platform.api.extension_models.app.Operation;
import network.piranya.platform.api.lang.None;
import network.piranya.platform.api.lang.Result;
import network.piranya.platform.api.lang.ResultHandler;
import network.piranya.platform.api.models.infrastructure.storage.KeyValueStore;

@AppServiceMetadata(id = "UserPrefsAppService")
public class UserPrefsAppService extends AppService {
	
	@Operation("bind_key")
	public void invoke(Keybind keybind, ResultHandler<None> resultHandler) {
		//System.out.println("bind key: " + context().storage().encodeJson(keybind));
		db().put("keybind:" + keybind.getHotkey(), context().serialization().encodeJson(keybind));
		resultHandler.accept(new Result<>(None.VALUE));
	}
	
	@Operation("get_user_prefs")
	public void invoke(Parameters params, ResultHandler<UserPrefs> resultHandler) {
		UserPrefs userPrefs = new UserPrefs();
		db().iterate("keybind:", (key, value) -> userPrefs.getKeybinds().add(context().serialization().decodeJson(value, Keybind.class)));
		//System.out.println("get_user_prefs: " + userPrefs.getKeybinds().size());
		resultHandler.accept(new Result<>(userPrefs));
	}
	
	// TODO updateRefCurrency
	
	
	protected KeyValueStore db() {
		return context().storage().globalKeyValue("prefs");
	}
	
	
	public static class Keybind {
		
		private String hotkey;
		public String getHotkey() {
			return hotkey;
		}
		public void setHotkey(String hotkey) {
			this.hotkey = hotkey;
		}
		
		private String actionDescriptor;
		public String getActionDescriptor() {
			return actionDescriptor;
		}
		public void setActionDescriptor(String actionDescriptor) {
			this.actionDescriptor = actionDescriptor;
		}
		
		private boolean invokeDirectly;
		public boolean isInvokeDirectly() {
			return invokeDirectly;
		}
		public void setInvokeDirectly(boolean invokeDirectly) {
			this.invokeDirectly = invokeDirectly;
		}
		
		private Map<String, String> params;
		public Map<String, String> getParams() {
			return params;
		}
		public void setParams(Map<String, String> params) {
			this.params = params;
		}
		
		public Keybind() {
		}
	}
	
	public class UserPrefs {
		
		// TODO String refCurrency
		
		private final List<Keybind> keybinds = utils.col.list();
		public List<Keybind> getKeybinds() {
			return keybinds;
		}
		
		public UserPrefs() {
		}
	}
	
}
