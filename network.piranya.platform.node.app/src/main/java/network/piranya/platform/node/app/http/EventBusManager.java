package network.piranya.platform.node.app.http;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

public class EventBusManager {
	
	public void dispose() {
		
	}
	
	
	protected void init() {
		BridgeOptions options = new BridgeOptions().addOutboundPermitted(new PermittedOptions().setAddressRegex("\\.+"));
		SockJSHandler.create(vertx()).bridge(options, event -> {
	         if (event.type() == BridgeEventType.SOCKET_CREATED) {
	            //logger.info("A socket was created");
	        }
	        event.complete(true);
	    });

	}
	
	public EventBusManager(Vertx vertx) {
		this.vertx = vertx;
		this.eventBus = vertx.eventBus();
	}
	
	private final Vertx vertx;
	protected Vertx vertx() {
		return vertx;
	}
	
	private final EventBus eventBus;
	protected EventBus getEventBus() {
		return eventBus;
	}
	
}
