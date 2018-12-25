package network.piranya.platform.api.extension_models.app;

import java.util.function.BiConsumer;

import network.piranya.platform.api.extension_models.ManagerialExtensionContext;
import network.piranya.platform.api.utilities.Utilities;

public abstract class AppService {
	
	protected void init() {
	}
	
	
	protected final void publishEvent(String eventTypeName, Object jsonifiableAppEvent) {
		appEventsPublisher.accept(eventTypeName, jsonifiableAppEvent);
	}
	
	
	protected final ManagerialExtensionContext context() {
		return this.context;
	}
	/// injected
	private ManagerialExtensionContext context;
	
	/// injected
	private BiConsumer<String, Object> appEventsPublisher;
	
	
	protected final Utilities utils = new Utilities();
	protected final Utilities utils() { return utils; }
	
}
