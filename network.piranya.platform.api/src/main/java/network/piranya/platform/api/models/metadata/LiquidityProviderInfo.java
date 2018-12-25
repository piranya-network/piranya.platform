package network.piranya.platform.api.models.metadata;

import java.util.Map;

public class LiquidityProviderInfo {
	
	private final String id;
	public String getId() {
		return id;
	}
	
	private final String lpTypeId;
	public String getLpTypeId() {
		return lpTypeId;
	}
	
	private final String displayName;
	public String getDisplayName() {
		return displayName;
	}
	
	private final String description;
	public String getDescription() {
		return description;
	}
	
	private final Map<String, Object> params;
	public Map<String, Object> getParams() {
		return params;
	}
	
	/*
	private final boolean isOrderReferenceSupported;
	public boolean isOrderReferenceSupported() {
		return isOrderReferenceSupported;
	}
	
	private final boolean areTpSlSupported;
	public boolean areTpSlSupported() {
		return areTpSlSupported;
	}
	*/
	
	public LiquidityProviderInfo(String id, String lpTypeId, String displayName, String description, Map<String, Object> params) {
		this.id = id;
		this.lpTypeId = lpTypeId;
		this.displayName = displayName;
		this.description = description;
		this.params = params;
	}
	
}
