package network.piranya.platform.node.api.modules;

import network.piranya.platform.api.Version;

public class ModuleMetadata implements Comparable<ModuleMetadata> {
	
	private final String moduleId;
	public String moduleId() {
		return moduleId;
	}
	
	private final Version version;
	public Version version() {
		return version;
	}
	
	private final String displayName;
	public String displayName() {
		return displayName;
	}
	
	private final String description;
	public String description() {
		return description;
	}
	
	private final String uniqueModuleId;
	public String uniqueModuleId() {
		return uniqueModuleId;
	}
	
	public ModuleMetadata(String moduleId, Version version, String displayName, String description) {
		this.moduleId = moduleId;
		this.version = version;
		this.displayName = displayName;
		this.description = description;
		this.uniqueModuleId = String.format("%s:%s", moduleId, version);
	}
	
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof ModuleMetadata) {
			ModuleMetadata o = (ModuleMetadata)other;
			return moduleId().equals(o.moduleId()) && version().equals(o.version());
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return String.format("%s:%s", moduleId(), version()).hashCode();
	}
	
	@Override
	public int compareTo(ModuleMetadata o) {
		int diff = moduleId().compareTo(o.moduleId());
		return diff != 0 ? diff : o.version().compareTo(version());
	}
	
}
