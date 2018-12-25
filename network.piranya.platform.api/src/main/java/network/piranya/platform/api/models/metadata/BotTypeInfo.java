package network.piranya.platform.api.models.metadata;

public class BotTypeInfo {
	
	private final String botTypeId;
	public String getBotTypeId() {
		return botTypeId;
	}
	
	private final Type type;
	public Type getType() {
		return type;
	}
	
	private final String displayName;
	public String getDisplayName() {
		return displayName;
	}
	private final String description;
	public String getDescription() {
		return description;
	}
	
	private final ModuleInfo moduleInfo;
	public ModuleInfo getModuleInfo() {
		return moduleInfo;
	}
	
	private final boolean isSingleton;
	public boolean isSingleton() {
		return isSingleton;
	}
	
	private final String[] features;
	public String[] getFeatures() {
		return features;
	}
	
	private final CommandInfo[] commands;
	public CommandInfo[] getCommands() {
		return commands;
	}
	
	private final InputInfo[] inputs;
	public InputInfo[] getInputs() {
		return inputs;
	}
	
	private final SearchInfo searchInfo;
	public SearchInfo getSearchInfo() {
		return searchInfo;
	}
	
	public BotTypeInfo(String botTypeId, Type type, String displayName, String description, ModuleInfo moduleInfo, boolean isSingleton, String[] features,
			CommandInfo[] commands, InputInfo[] inputs, SearchInfo searchInfo) {
		this.botTypeId = botTypeId;
		this.type = type;
		this.displayName = displayName;
		this.description = description;
		this.moduleInfo = moduleInfo;
		this.isSingleton = isSingleton;
		this.features = features;
		this.commands = commands;
		this.inputs = inputs;
		this.searchInfo = searchInfo;
	}
	
	public static enum Type { COMMAND, EXECUTION }
	
}
