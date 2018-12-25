package network.piranya.platform.node.core.execution.engine.bots;

import java.util.function.Consumer;
import java.util.function.Predicate;

import network.piranya.platform.api.models.trading.ExecutionEvent;

public class BotEventSubscription {
	
	private final RunningBot runningBot;
	public RunningBot runningBot() {
		return runningBot;
	}
	
	private final Class<ExecutionEvent> eventType;
	public Class<ExecutionEvent> eventType() {
		return eventType;
	}
	
	private final Predicate<ExecutionEvent> predicate;
	public Predicate<ExecutionEvent> predicate() {
		return predicate;
	}
	
	private final Consumer<ExecutionEvent> consumer;
	public Consumer<ExecutionEvent> consumer() {
		return consumer;
	}
	
	private final boolean hasPredicate;
	public boolean hasPredicate() {
		return hasPredicate;
	}
	
	private final boolean demandsOnReplay;
	public boolean isDemandsOnReplay() {
		return demandsOnReplay;
	}
	
	public BotEventSubscription(RunningBot runningBot, Class<ExecutionEvent> eventType, Predicate<ExecutionEvent> predicate,
			boolean hasPredicate, Consumer<ExecutionEvent> consumer, boolean demandsOnReplay) {
		this.runningBot = runningBot;
		this.eventType = eventType;
		this.predicate = predicate;
		this.hasPredicate = hasPredicate;
		this.consumer = consumer;
		this.demandsOnReplay = demandsOnReplay;
	}
	
}
