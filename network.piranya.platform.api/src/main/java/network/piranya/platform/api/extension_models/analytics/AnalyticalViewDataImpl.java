package network.piranya.platform.api.extension_models.analytics;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import network.piranya.platform.api.models.analytics.AnalyticalViewData;

public class AnalyticalViewDataImpl<DataType> implements AnalyticalViewData<DataType> {
	
	@Override
	public DataType data() {
		return data;
	}
	
	public void updateData(DataType data) {
		this.data = data;
		publishUpdate();
	}
	
	private DataType data;
	
	@Override
	public void subscribe(Consumer<DataType> subscriber) {
		subscribers.put(subscriber, true);
	}
	
	@Override
	public void unsubscribe(Consumer<DataType> subscriber) {
		subscribers.remove(subscriber);
	}
	
	protected void publishUpdate() {
		subscribers.keySet().stream().forEach(subscriber -> {
			try { subscriber.accept(data()); }
			catch (Throwable ex) {}
		});
	}
	
	private final ConcurrentMap<Consumer<DataType>, Boolean> subscribers = new ConcurrentHashMap<>();
	
}
