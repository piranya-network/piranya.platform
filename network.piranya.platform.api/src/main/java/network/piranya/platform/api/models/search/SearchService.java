package network.piranya.platform.api.models.search;

import java.util.function.Consumer;

public interface SearchService {
	
	void search(SearchQuery query, Consumer<SearchResult> resultHandler);
	
}
