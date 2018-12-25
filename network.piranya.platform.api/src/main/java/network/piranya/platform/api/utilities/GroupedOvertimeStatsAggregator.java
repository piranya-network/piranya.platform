package network.piranya.platform.api.utilities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import network.piranya.platform.api.lang.TimeWindowUnit;

public class GroupedOvertimeStatsAggregator<Entry, AggregateItem> {
	
	public void accept(Entry entry) {
		String groupId = groupIdProvider.apply(entry);
		
		OvertimeStatsAggregator<Entry, AggregateItem> aggregator = null;
		
		for (Map.Entry<String, OvertimeStatsAggregator<Entry, AggregateItem>> e : aggregatorsMap.entrySet()) {
			if (e.getKey().equals(groupId)) {
				aggregator = e.getValue();
			} else {
				e.getValue().catchup(timeExtractor.apply(entry));
			}
		}
		
		if (aggregator == null) {
			aggregator = new OvertimeStatsAggregator<>(intervalTimeUnit, interval, maxItemsCount, initialEndTime, timeExtractor, aggregateItemFactory, feedAggregate);
			aggregatorsMap.put(groupId, aggregator);
		}
		
		aggregator.accept(entry);
	}
	
	public Map<String, List<AggregateItem>> aggregatesMap() {
		Map<String, List<AggregateItem>> result = new HashMap<>();
		for (Map.Entry<String, OvertimeStatsAggregator<Entry, AggregateItem>> e : aggregatorsMap.entrySet()) {
			result.put(e.getKey(), e.getValue().aggregates());
		}
		return result;
	}
	
	
	GroupedOvertimeStatsAggregator(TimeWindowUnit intervalTimeUnit, int interval, int maxItemsCount, long initialEndTime,
			Function<Entry, Long> timeExtractor, Function<Long, AggregateItem> aggregateItemFactory, BiConsumer<Entry, AggregateItem> feedAggregate,
			Function<Entry, String> groupIdProvider) {
		this.intervalTimeUnit = intervalTimeUnit;
		this.interval = interval;
		this.maxItemsCount = maxItemsCount;
		this.initialEndTime = initialEndTime;
		this.timeExtractor = timeExtractor;
		this.aggregateItemFactory = aggregateItemFactory;
		this.feedAggregate = feedAggregate;
		this.groupIdProvider = groupIdProvider;
	}
	
	private final TimeWindowUnit intervalTimeUnit;
	private final int interval;
	private final int maxItemsCount;
	private final long initialEndTime;
	private final Function<Entry, Long> timeExtractor;
	private final Function<Long, AggregateItem> aggregateItemFactory;
	private final BiConsumer<Entry, AggregateItem> feedAggregate;
	private Function<Entry, String> groupIdProvider;
	
	private final Map<String, OvertimeStatsAggregator<Entry, AggregateItem>> aggregatorsMap = new HashMap<>();
	
}
