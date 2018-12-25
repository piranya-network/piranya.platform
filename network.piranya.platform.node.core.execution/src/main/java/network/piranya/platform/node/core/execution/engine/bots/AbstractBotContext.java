package network.piranya.platform.node.core.execution.engine.bots;

import static network.piranya.platform.node.utilities.ReflectionUtils.findMethod;
import static network.piranya.platform.node.utilities.ReflectionUtils.invoke;

import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import network.piranya.platform.api.exceptions.PiranyaException;
import network.piranya.platform.api.extension_models.execution.bots.ExecutionBot;
import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.api.extension_models.execution.bots.EventProcessor;
import network.piranya.platform.api.models.bots.BotRef;
import network.piranya.platform.api.models.bots.BotsListing;
import network.piranya.platform.api.models.trading.ExecutionEvent;
import network.piranya.platform.node.api.execution.bots.BotsRegistry;
import network.piranya.platform.node.core.execution.engine.bots.BotsOperator.ExecutionContextImpl;

public abstract class AbstractBotContext implements AutoCloseable {
	
	protected abstract ExecutionContextImpl newExecutionContext();
	
	public BotsListing bots() {
		checkIfDisposed();
		
		return this.botsListing;
	}
	
	public void finish() {
		checkIfDisposed();
		
		botFinisher().accept(bot().bot().ref(), Optional.empty());
	}
	
	public void finishWithError(Exception error) {
		checkIfDisposed();
		
		botFinisher().accept(bot().bot().ref(), Optional.of(error));
	}
	
	
	public <Event extends ExecutionEvent> void subscribe(Class<Event> eventType) {
		checkIfDisposed();
		
		subscribe(eventType, event -> true, true, false);
	}
	
	public <Event extends ExecutionEvent> void subscribe(Class<Event> eventType, Predicate<Event> predicate) {
		checkIfDisposed();
		
		subscribe(eventType, predicate, true, true);
	}
	
	@SuppressWarnings("unchecked")
	public <Event extends ExecutionEvent> void subscribe(Class<Event> eventType, Predicate<Event> predicate, boolean requireMethod, boolean hasPredicate) {
		checkIfDisposed();
		
		Class<ExecutionEvent> subscribedEventType = (Class<ExecutionEvent>)eventType;
		
		final Optional<Method> advancedProcessorMethod = findMethod(bot().bot().getClass(), EventProcessor.class, subscribedEventType, ExecutionBot.ExecutionContext.class);
		Consumer<ExecutionEvent> consumer = null;
		boolean demandsOnReplay = true;
		if (advancedProcessorMethod.isPresent()) {
			demandsOnReplay = advancedProcessorMethod.get().getAnnotation(EventProcessor.class).demandOnReplay();
			consumer = event -> {
				try (BotsOperator.ExecutionContextImpl executionContext = newExecutionContext()) {
					invoke(bot().bot(), advancedProcessorMethod.get(), event, executionContext);
				}
			};
		} else {
			Optional<Method> processorMethod = findMethod(bot().bot().getClass(), EventProcessor.class, subscribedEventType);
			if (processorMethod.isPresent()) {
				demandsOnReplay = processorMethod.get().getAnnotation(EventProcessor.class).demandOnReplay();
				consumer = event -> invoke(bot().bot(), processorMethod.get(), event);
			} else if (requireMethod) {
				throw new PiranyaException(String.format("Event Processor method for event '%s' was not found", subscribedEventType.getSimpleName()));
			}
		}
		if (consumer != null) {
			botEventSubscriber().accept(new BotEventSubscription(bot(), (Class<ExecutionEvent>)subscribedEventType,
					(Predicate<ExecutionEvent>)predicate, hasPredicate, consumer, demandsOnReplay));
		}
	}
	
	public void checkIfDisposed() {
		if (disposed) {
			throw new PiranyaException("Context is disposed");
		}
	}
	
	public void dispose() {
		this.disposed = true;
		botsListing.dispose();
	}
	private boolean disposed = false;
	
	@Override
	public void close() {
		dispose();
	}
	
	
	public AbstractBotContext(RunningBot bot, BotsRegistry agentsRegistry, Consumer<BotEventSubscription> botEventSubscriber) {
		this.bot = bot;
		this.botEventSubscriber = botEventSubscriber;
		this.botsListing = new BotsListingImpl(agentsRegistry);
	}
	
	public void init(BiConsumer<BotRef, Optional<Exception>> actorFinisher) {
		this.botFinisher = actorFinisher;
	}
	
	private final RunningBot bot;
	protected RunningBot bot() {
		return bot;
	}
	
	private final Consumer<BotEventSubscription> botEventSubscriber;
	protected Consumer<BotEventSubscription> botEventSubscriber() {
		return botEventSubscriber;
	}
	
	private final BotsListingImpl botsListing;
	
	private BiConsumer<BotRef, Optional<Exception>> botFinisher;
	protected BiConsumer<BotRef, Optional<Exception>> botFinisher() {
		return botFinisher;
	}
	
}
