package network.piranya.platform.api.models.search;

public class SearchResult {
	
	private final SearchResultEntry[] entries;
	public SearchResultEntry[] getEntries() {
		return entries;
	}
	
	public SearchResult(SearchResultEntry[] entries) {
		this.entries = entries;
	}
	
}
