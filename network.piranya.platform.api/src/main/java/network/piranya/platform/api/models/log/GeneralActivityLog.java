package network.piranya.platform.api.models.log;

import java.util.Map;

public class GeneralActivityLog extends ActivityLog {
	
	private final String entryType;
	public String getEntryType() {
		return entryType;
	}
	
	private final String description;
	public String getDescription() {
		return description;
	}
	
	private final Map<String, String> extra;
	public Map<String, String> getExtra() {
		return extra;
	}
	
	public GeneralActivityLog(String entryType, String description, Map<String, String> extra, long time) {
		super(time);
		this.entryType = entryType;
		this.description = description;
		this.extra = extra;
	}
	
}
