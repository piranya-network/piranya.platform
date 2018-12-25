package network.piranya.platform.api.extension_models.execution.bots;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import network.piranya.platform.api.lang.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import network.piranya.platform.api.extension_models.Data;
import network.piranya.platform.api.extension_models.ExtensionContext;
import network.piranya.platform.api.extension_models.Parameters;
import network.piranya.platform.api.extension_models.execution.sandbox.SandboxConfiguration;
import network.piranya.platform.api.lang.ReplyHandler;
import network.piranya.platform.api.models.bots.BotRef;
import network.piranya.platform.api.models.bots.BotView;
import network.piranya.platform.api.models.bots.BotsListing;
import network.piranya.platform.api.models.execution.sandbox.Sandbox;
import network.piranya.platform.api.models.infrastructure.SerializationServices;
import network.piranya.platform.api.models.infrastructure.storage.StorageServices;
import network.piranya.platform.api.models.trading.ExecutionEvent;
import network.piranya.platform.api.models.trading.TradingState;
import network.piranya.platform.api.models.trading.liquidity.LiquidityProvidersListing;
import network.piranya.platform.api.utilities.Utilities;

/**
 * A Transactional Bot that is run within the Execution Engine.
 * Execution Bots are event-sourced, their state isn't saved, it's replayed after restart from events and commands that composed them.
 *
 */
public abstract class ExecutionBot extends Bot {
	
	public void onStart(ExecutionContext context) {
		
	}
	
	public void onAbort(ExecutionContext executionContext) {
		
	}
	
	public void onFinish(ExecutionContext executionContext) {
		
	}
	
	
	/// fields injected by execution engine
	private BotRef ref;
	private Parameters params;
	private Consumer<BotEvent> eventsListener;
	private Consumer<Consumer<BotEvent>> subscribeOperation;
	private Consumer<Consumer<BotEvent>> unsubscribeOperation;
	
	public final Parameters params() {
		return params;
	}
	protected final void updateParams(Parameters params) {
		this.params = params;
		this.eventsListener.accept(new BotEvent(ref(), getClass().getName(), BotEvent.PARAMS_UPDATED_EVENT_TYPE_ID, params));
	}
	
	
	protected final void publishEvent(String eventTypeId, Data data) {
		this.eventsListener.accept(new BotEvent(ref(), getClass().getName(), eventTypeId, data));
	}
	
	protected final void publishEvent(String eventTypeId, Data data, String errorDescription) {
		this.eventsListener.accept(new BotEvent(ref(), getClass().getName(), eventTypeId, Optional.of(errorDescription), data));
	}
	
	
	public final BotRef ref() {
		return this.ref;
	}
	
	public final BotView view() {
		return view;
	}
	private BotView view;
	
	protected final void updateState(Data state) {
		// TODO validate that within context. consider in RunningBot
		this.state = state;
	}
	private Data state = new Data();
	
	protected final void updateLabel(String label) {
		this.label = label;
	}
	private String label;
	
	protected final void updateDescription(String description) {
		this.description = description;
	}
	private String description;
	
	protected final void updateTypedState(Object state) {
		//
		this.typedStates.put(state.getClass().getName(), state);
	}
	private Map<String, Object> typedStates = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	private final <T> T getTypedState(String stateType) {
		Object state = typedStates.get(stateType);
		if (state != null) {
			return (T)state;
		}
		
		if (stateType.indexOf('.') < 0) {
			/// filter state types that have same simple class name (with no package) and sort them as strings (later versions will have larger values, hence at end of list)
			List<String> list = utils().col().sort(utils().col().filter(typedStates.keySet(), st -> st.endsWith("." + stateType)));
			if (list.size() > 0) {
				return (T)typedStates.get(list.get(list.size() - 1));
			}
		}
		
		throw new UnprovidedBotStateException(getClass(), stateType);
	}
	
	
	protected final Utilities utils = new Utilities();
	protected Utilities utils() { return utils; }
	
	
	public ExecutionBot() {
		this.view = new BotView() {
			@Override public BotRef ref() { return ref; }
			
			@Override public String botTypeId() { return ExecutionBot.this.getClass().getName(); }
			
			@Override public String label() {
				return label != null ? label : String.format("%s [%s]", ExecutionBot.this.getClass().getDeclaredAnnotation(BotMetadata.class).displayName(), ref());
			}
			
			@Override public String description() {
				return description != null ? description : "";
			}
			
			@Override public Parameters params() { return ExecutionBot.this.params(); }
			
			@Override public Data state() { return state; }
			
			@Override public <T> T state(String stateType) { return getTypedState(stateType); }
			
			@Override public <T> T state(Class<T> stateClass) { return getTypedState(stateClass.getName()); }
			
			@Override public void subscribe(Consumer<BotEvent> listener) { subscribeOperation.accept(listener); }
			
			@Override public void unsubscribe(Consumer<BotEvent> listener) { unsubscribeOperation.accept(listener); }
			
			@SuppressWarnings("unchecked")
			@Override public <QueryInterface> QueryInterface query(Class<QueryInterface> queryInterface) { return (QueryInterface)ExecutionBot.this; }
		};
	}
	
	
	public static interface Context {
		
	}
	
	public static interface ExecutionContext extends Context {
		
		TradingState tradingState();
		
		BotsListing bots();
		
		void finish();
		void finishWithError(Exception error);
		
		<SandboxClass extends SandboxConfiguration> Sandbox createSandbox(SandboxClass sandboxConfig);
		
		<Event extends ExecutionEvent> void subscribe(Class<Event> eventType);
		<Event extends ExecutionEvent> void subscribe(Class<Event> eventType, Predicate<Event> predicate);
		
		ReplyHandler<BotRef> forkBot(ForkBotParameters params);
		
		LiquidityProvidersListing<ExecutionContext> liquidityProviders();
		
		StorageServices storage();
		SerializationServices serialization();
		
		DetachedContext acquireDetachedContext();
		
	}
	
	public static interface DetachedContext extends Context, ExtensionContext {
		
		void dispose();
	}
	
	public static class ForkBotParameters {
		
		private final String botClassName;
		public String botClassName() {
			return botClassName;
		}
		
		private final Parameters params;
		public Parameters params() {
			return params;
		}
		
		public ForkBotParameters(String botClassName, Parameters params) {
			this.botClassName = botClassName;
			this.params = params;
		}
	}
	
	// straight line case
	public static class Metadata {
		
	}
	
}
