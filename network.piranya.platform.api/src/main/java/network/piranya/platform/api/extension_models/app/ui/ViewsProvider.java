package network.piranya.platform.api.extension_models.app.ui;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import network.piranya.platform.api.extension_models.ManagerialExtensionContext;
import network.piranya.platform.api.lang.ResultHandler;
import network.piranya.platform.api.utilities.Utilities;

public abstract class ViewsProvider {
	
	public abstract String viewType();
	
	
	public abstract void init();
	
	public abstract void dispose();
	
	public abstract void listViews(Consumer<List<View>> resultHandler);
	
	public void watchViews(List<String> viewsIds, ResultHandler<List<View>> resultHandler) {
	}
	
	public void unwatchViews(List<String> viewsIds) {
	}
	
	public void getView(String viewId, ResultHandler<View> resultHandler) {
	}
	
	public final void subscribe(Consumer<ViewEvent> subscriber) {
		subscribers().put(subscriber, true);
	}
	
	public final void unsubscribe(Consumer<ViewEvent> subscriber) {
		subscribers().remove(subscriber);
	}
	
	protected final void publish(ViewEvent event) {
		for (Consumer<ViewEvent> subscriber : subscribers().keySet()) {
			try { subscriber.accept(event); }
			catch (Exception ex) { }
		}
	}
	
	protected final ManagerialExtensionContext context() {
		return context;
	}
	private ManagerialExtensionContext context;
	
	
	protected final Utilities utils = new Utilities();
	protected Utilities utils() { return utils; }
	
	
	private final ConcurrentMap<Consumer<ViewEvent>, Boolean> subscribers = new ConcurrentHashMap<>();
	private ConcurrentMap<Consumer<ViewEvent>, Boolean> subscribers() {
		return subscribers;
	}
	
	
	public static class ViewEvent {
		
		public static enum EventType { ADDED, UPDATED, REMOVED, UPDATE_INSTRUCTIONS }
		
		public ViewEvent(View view, EventType eventType) {
			this.view = view;
			this.eventType = eventType;
		}
		
		private final View view;
		public View getView() {
			return view;
		}
		
		private final EventType eventType;
		public EventType getEventType() {
			return eventType;
		}
	}
	
}
