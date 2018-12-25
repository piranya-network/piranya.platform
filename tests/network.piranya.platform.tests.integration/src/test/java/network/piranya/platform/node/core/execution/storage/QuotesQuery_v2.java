package network.piranya.platform.node.core.execution.storage;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public interface QuotesQuery_v2 {
	
	List<Quote> queryQuotes(long startTime, long endTime);
	
	void queryQuotes(long startTime, long endTime, String symbol, Consumer<Quote> consumer);
	
	Iterator<Object> iterateQuotes();
	
}
