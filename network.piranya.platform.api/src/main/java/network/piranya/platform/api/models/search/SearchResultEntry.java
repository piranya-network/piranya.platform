package network.piranya.platform.api.models.search;

public class SearchResultEntry {
	
	private final String title;
	public String getTitle() {
		return title;
	}
	
	private final EntryType entryType;
	public EntryType getEntryType() {
		return entryType;
	}
	
	private final String description;
	public String getDescription() {
		return description;
	}
	
	private final SearchAction[] actions;
	public SearchAction[] getActions() {
		return actions;
	}
	
	public SearchResultEntry(String title, EntryType entryType, String description, SearchAction[] actions) {
		this.title = title;
		this.entryType = entryType;
		this.description = description;
		this.actions = actions;
	}
	
	
	public static enum EntryType { BOT, UI_ACTION, PAGE, LIST, CHART, LIQUIDITY_PROVIDER, LIQUIDITY_PROVIDER_CEX, LIQUIDITY_PROVIDER_DEX, ASSET, INSTRUMENT, COMMUNITY, GLOBAL }
	
}
