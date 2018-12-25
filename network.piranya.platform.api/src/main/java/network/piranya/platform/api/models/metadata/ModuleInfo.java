package network.piranya.platform.api.models.metadata;

public class ModuleInfo implements Comparable<ModuleInfo> {
	
	private final String moduleId;
	public String getModuleId() {
		return moduleId;
	}
	
	private final String version;
	public String getVersion() {
		return version;
	}
	
	private final String displayName;
	public String getDisplayName() {
		return displayName;
	}
	
	private final String description;
	public String getDescription() {
		return description;
	}
	
	private final String uniqueModuleId;
	public String getUniqueModuleId() {
		return uniqueModuleId;
	}
	
	public ModuleInfo(String moduleId, String version, String displayName, String description, String uniqueModuleId) {
		this.moduleId = moduleId;
		this.version = version;
		this.displayName = displayName;
		this.description = description;
		this.uniqueModuleId = uniqueModuleId;
	}
	
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof ModuleInfo) {
			ModuleInfo o = (ModuleInfo)other;
			return getModuleId().equals(o.getModuleId()) && getVersion().equals(o.getVersion());
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return String.format("%s:%s", getModuleId(), getVersion()).hashCode();
	}
	
	@Override
	public int compareTo(ModuleInfo o) {
		int diff = getModuleId().compareTo(o.getModuleId());
		return diff != 0 ? diff : o.getVersion().compareTo(getVersion());
	}
	
}
