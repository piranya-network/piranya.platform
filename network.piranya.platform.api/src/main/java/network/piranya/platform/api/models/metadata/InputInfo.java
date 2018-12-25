package network.piranya.platform.api.models.metadata;

import java.util.Arrays;

import network.piranya.platform.api.extension_models.Action;
import network.piranya.platform.api.extension_models.Input;
import network.piranya.platform.api.extension_models.InputType;
import network.piranya.platform.api.utilities.CollectionUtils;

public class InputInfo {
	
	private String id;
	public String getId() {
		return id;
	}
	
	private String displayName;
	public String getDisplayName() {
		return displayName;
	}
	
	private InputType inputType;
	public InputType getInputType() {
		return inputType;
	}
	
	private String defaultValue;
	public String getDefaultValue() {
		return defaultValue;
	}
	
	private int minEntries;
	public int getMinEntries() {
		return minEntries;
	}
	
	private int maxEntries;
	public int getMaxEntries() {
		return maxEntries;
	}
	
	private String constraints;
	public String getConstraints() {
		return constraints;
	}
	
	private String description;
	public String getDescription() {
		return description;
	}
	
	private boolean savable;
	public boolean isSavable() {
		return savable;
	}
	
	private boolean saveByDefault;
	public boolean isSaveByDefault() {
		return saveByDefault;
	}
	
	private String savedValue;
	public String getSavedValue() {
		return savedValue;
	}
	
	private final InputActionInfo[] actions;
	public InputActionInfo[] getActions() {
		return actions;
	}
	
	public InputInfo(String id, String displayName, InputType inputType, String defaultValue, int minEntries, int maxEntries, String constraints, String description,
			boolean savable, boolean saveByDefault, String savedValue, InputActionInfo[] actions) {
		this.id = id;
		this.displayName = displayName;
		this.inputType = inputType;
		this.defaultValue = defaultValue;
		this.minEntries = minEntries;
		this.maxEntries = maxEntries;
		this.constraints = constraints;
		this.description = description;
		this.savable = savable;
		this.saveByDefault = saveByDefault;
		this.savedValue = savedValue;
		this.actions = actions;
	}
	
	public InputInfo(Input input) {
		this(input.id(), input.displayName(), input.type(), input.defaultValue(), input.minEntries(), input.maxEntries(),
				input.constraints(), input.description(), input.savable(), input.saveByDefault(), null, getActionInfos(input.actions()));
	}
	
	protected static InputActionInfo[] getActionInfos(Action[] actions) {
		return new CollectionUtils().map(Arrays.asList(actions), action -> new InputActionInfo(action.actionDescriptor(), action.label())).toArray(new InputActionInfo[0]);
	}
	
}
