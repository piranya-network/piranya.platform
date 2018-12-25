package network.piranya.platform.node.core.execution.storage;

import java.util.List;

public interface QuotesQuery {
	
	List<Quote> queryQuotes(long startTime, long endTime);
	
}
