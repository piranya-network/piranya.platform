package network.piranya.platform.api.utilities;

import java.util.function.BiConsumer;
import java.util.function.Function;

import network.piranya.platform.api.lang.TimeWindowUnit;

public class StatsUtils {
	
	public <Entry, AggregateItem> OvertimeStatsAggregator<Entry, AggregateItem> overtimeStatsAggregator(
			TimeWindowUnit intervalTimeUnit, int interval, int maxItemsCount, long initialEndTime,
			Function<Entry, Long> timeExtractor, Function<Long, AggregateItem> aggregateItemFactory, BiConsumer<Entry, AggregateItem> feedAggregate) {
		return new OvertimeStatsAggregator<>(intervalTimeUnit, interval, maxItemsCount, initialEndTime, timeExtractor, aggregateItemFactory, feedAggregate);
	}
	
	public <Entry, AggregateItem> GroupedOvertimeStatsAggregator<Entry, AggregateItem> groupedOvertimeStatsAggregators(
			TimeWindowUnit intervalTimeUnit, int interval, int maxItemsCount, long initialEndTime, Function<Entry, Long> timeExtractor,
			Function<Long, AggregateItem> aggregateItemFactory, BiConsumer<Entry, AggregateItem> feedAggregate, Function<Entry, String> groupIdProvider) {
		return new GroupedOvertimeStatsAggregator<>(intervalTimeUnit, interval, maxItemsCount, initialEndTime, timeExtractor, aggregateItemFactory, feedAggregate, groupIdProvider);
	}
	
	
	protected StatsUtils() { }
	
}
