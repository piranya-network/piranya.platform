package network.piranya.platform.api.models.metadata;

import network.piranya.platform.api.extension_models.execution.liquidity.LiquidityProviderCategory;

public class LiquidityProviderTypeInfo implements Comparable<LiquidityProviderTypeInfo> {
	
	private final String lpTypeId;
	public String getLpTypeId() {
		return lpTypeId;
	}
	
	private final String displayName;
	public String getDisplayName() {
		return displayName;
	}
	
	private final LiquidityProviderCategory lpCategory;
	public LiquidityProviderCategory getLpCategory() {
		return lpCategory;
	}
	
	private final String description;
	public String getDescription() {
		return description;
	}
	
	private final String[] features;
	public String[] getFeatures() {
		return features;
	}
	
	private final String[] searchTags;
	public String[] getSearchTags() {
		return searchTags;
	}
	
	private final InputInfo[] inputs;
	public InputInfo[] getInputs() {
		return inputs;
	}
	
	public LiquidityProviderTypeInfo(String lpTypeId, String displayName, LiquidityProviderCategory category, String description,
			String[] features, String[] searchTags, InputInfo[] inputs) {
		this.lpTypeId = lpTypeId;
		this.displayName = displayName;
		this.lpCategory = category;
		this.description = description;
		this.features = features;
		this.searchTags = searchTags;
		this.inputs = inputs;
	}
	
	@Override
	public int compareTo(LiquidityProviderTypeInfo o) {
		return getDisplayName().compareTo(o.getDisplayName());
	}
	
}
