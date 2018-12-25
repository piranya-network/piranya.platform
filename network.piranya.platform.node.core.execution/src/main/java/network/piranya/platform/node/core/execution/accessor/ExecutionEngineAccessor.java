package network.piranya.platform.node.core.execution.accessor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import network.piranya.platform.api.exceptions.PricesNotAvailableException;
import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.api.models.bots.BotsListing;
import network.piranya.platform.api.models.trading.ExecutionEvent;
import network.piranya.platform.api.models.trading.Instrument;
import network.piranya.platform.api.models.trading.Quote;
import network.piranya.platform.api.models.trading.TradingState;
import network.piranya.platform.node.api.execution.ExecutionManager;
import network.piranya.platform.node.api.execution.bots.BotsRegistry;
import network.piranya.platform.node.core.execution.engine.bots.BotsListingImpl;
import network.piranya.platform.node.utilities.CollectionUtils;

public abstract class ExecutionEngineAccessor {
	
	public BotsListing bots() {
		return agentsListing;
	}
	
	public TradingState tradingState() {
		return executionManager().tradingState();
	}
	
	public Quote quote(Instrument instrument) throws PricesNotAvailableException {
		return executionManager().quote(instrument);
	}
	
	public Optional<Quote> getQuote(Instrument instrument) {
		try { return Optional.of(quote(instrument)); }
		catch (PricesNotAvailableException ex) { return Optional.empty(); }
	}
	
	public void subscribe(Consumer<ExecutionEvent> subscriber) {
		executionManager().subscribe(subscriber);
		subscriptions.put((Consumer<ExecutionEvent>)subscriber, (Consumer<ExecutionEvent>)subscriber);
	}
	
	@SuppressWarnings("unchecked")
	public <EventType extends ExecutionEvent> void subscribe(Class<EventType> eventType, Consumer<EventType> subscriber) {
		FilteringSubscription subscription = new FilteringSubscription((Class<ExecutionEvent>)eventType, (Consumer<ExecutionEvent>)subscriber);
		executionManager().subscribe(subscription);
		subscriptions.put((Consumer<ExecutionEvent>)subscriber, subscription);
	}
	
	public <EventType extends ExecutionEvent> void unsubscribe(Consumer<EventType> subscriber) {
		Consumer<ExecutionEvent> subscription = subscriptions.get(subscriber);
		executionManager().unsubscribe(subscription);
		subscriptions.remove(subscriber);
	}
	
	public void dispose() {
		CollectionUtils.foreach(subscriptions.values(), s -> unsubscribe(s));
	}
	
	
	public ExecutionEngineAccessor(ExecutionManager executionManager, BotsRegistry agentsRegistry) {
		this.executionManager = executionManager;
		this.agentsListing = new BotsListingImpl(agentsRegistry);
	}
	
	private final ExecutionManager executionManager;
	protected ExecutionManager executionManager() {
		return executionManager;
	}
	
	private final BotsListingImpl agentsListing;
	
	private final ConcurrentMap<Consumer<ExecutionEvent>, Consumer<ExecutionEvent>> subscriptions = new ConcurrentHashMap<>();
	
	
	protected class FilteringSubscription implements Consumer<ExecutionEvent> {
		
		@Override
		public void accept(ExecutionEvent event) {
			if (eventType.isInstance(event)) {
				subscriber.accept(event);
			}
		}
		
		public FilteringSubscription(Class<ExecutionEvent> eventType, Consumer<ExecutionEvent> subscriber) {
			this.eventType = eventType;
			this.subscriber = subscriber;
		}
		
		private Class<ExecutionEvent> eventType;
		private final Consumer<ExecutionEvent> subscriber;
	}
	
}
