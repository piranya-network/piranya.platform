package network.piranya.platform.node.utilities;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

public class EventsSubscriptionSupport<EventType> {
	
	public void subscribe(Consumer<EventType> subscriber) {
		subscribers.put(subscriber, true);
	}
	
	public void unsubscribe(Consumer<EventType> subscriber) {
		subscribers.remove(subscriber);
	}
	
	public void publish(EventType event, boolean ignoreException) {
		for (Consumer<EventType> subscriber : subscribers.keySet()) {
			if (ignoreException) {
				try { subscriber.accept(event); }
				catch (Throwable ex) { }
			} else {
				subscriber.accept(event);
			}
		}
	}
	
	
	private final ConcurrentMap<Consumer<EventType>, Boolean> subscribers = new ConcurrentHashMap<>();
	
}
