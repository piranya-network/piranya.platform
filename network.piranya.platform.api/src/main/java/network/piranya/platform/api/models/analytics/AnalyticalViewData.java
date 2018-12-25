package network.piranya.platform.api.models.analytics;

import java.util.function.Consumer;

public interface AnalyticalViewData<DataType> {
	
	DataType data();
	
	void subscribe(Consumer<DataType> subscriber);
	void unsubscribe(Consumer<DataType> subscriber);
	
}
