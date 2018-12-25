package network.piranya.platform.api.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import network.piranya.platform.api.lang.TimeWindow;
import network.piranya.platform.api.lang.TimeWindowUnit;

public class OvertimeStatsAggregator<Entry, AggregateItem> {
	
	public void accept(Entry entry) {
		long time = timeExtractor.apply(entry);
		if (time < aggregates.get(currentAggIndex).window.startTime()) {
			return;
		}
		
		catchup(time);
		
		feedAggregate.accept(entry, aggregates.get(currentAggIndex).item);
	}
	
	void catchup(long time) {
		while (time > aggregates.get(currentAggIndex).window.endTime()) {
			if (currentAggIndex == aggregates.size() - 1) {
				TimeWindow window = dateTime.calcTimeWindow(aggregates.get(currentAggIndex).window.endTime() + 1, intervalTimeUnit, interval);
				aggregates.add(new AggEntryDetails(aggregateItemFactory.apply(window.startTime()), window));
				aggregates.remove(0);
			} else {
				++currentAggIndex;
			}
		}
	}
	
	public List<AggregateItem> aggregates() {
		return col.map(aggregates, a -> a.item);
	}
	
	OvertimeStatsAggregator(TimeWindowUnit intervalTimeUnit, int interval, int maxItemsCount, long initialEndTime,
			Function<Entry, Long> timeExtractor, Function<Long, AggregateItem> aggregateItemFactory, BiConsumer<Entry, AggregateItem> feedAggregate) {
		this.intervalTimeUnit = intervalTimeUnit;
		this.interval = interval;
		this.maxItemsCount = maxItemsCount;
		this.initialEndTime = initialEndTime;
		this.timeExtractor = timeExtractor;
		this.aggregateItemFactory = aggregateItemFactory;
		this.feedAggregate = feedAggregate;
		
		init();
	}
	
	protected void init() {
		TimeWindow window = null;
		for (int i = 0; i < maxItemsCount; i++) {
			window = dateTime.calcTimeWindow((window != null ? window.startTime() : initialEndTime) - 1, intervalTimeUnit, interval);
			AggregateItem item = aggregateItemFactory.apply(window.startTime());
			aggregates.add(0, new AggEntryDetails(item, window));
		}
	}
	
	private final TimeWindowUnit intervalTimeUnit;
	private final int interval;
	private final int maxItemsCount;
	private final long initialEndTime;
	
	private int currentAggIndex = 0;
	private final List<AggEntryDetails> aggregates = new ArrayList<>();
	
	private final Function<Entry, Long> timeExtractor;
	private final Function<Long, AggregateItem> aggregateItemFactory;
	private final BiConsumer<Entry, AggregateItem> feedAggregate;
	
	private final DateTimeUtils dateTime = new DateTimeUtils();
	private final CollectionUtils col = new CollectionUtils();
	
	
	protected class AggEntryDetails {
		
		public final AggregateItem item;
		public final TimeWindow window;
		
		public AggEntryDetails(AggregateItem item, TimeWindow window) {
			this.item = item;
			this.window = window;
		}
	}
	
}
