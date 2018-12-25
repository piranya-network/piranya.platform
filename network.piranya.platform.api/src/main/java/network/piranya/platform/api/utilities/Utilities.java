package network.piranya.platform.api.utilities;

import network.piranya.platform.api.extension_models.ParametersBuilder;

public class Utilities {
	
	public final OrderingUtils ordering = new OrderingUtils();
	public OrderingUtils ordering() { return ordering; }
	
	public final CollectionUtils col = new CollectionUtils();
	public CollectionUtils col() { return col; }
	
	public final DateTimeUtils datetime = new DateTimeUtils();
	public DateTimeUtils datetime() { return datetime; }
	
	public final Calculations calc = new Calculations();
	public Calculations calc() { return calc; }
	
	public final StatsUtils stats = new StatsUtils();
	public StatsUtils stats() { return stats; }
	
	public ParametersBuilder params() {
		return new ParametersBuilder();
	}
	
}
