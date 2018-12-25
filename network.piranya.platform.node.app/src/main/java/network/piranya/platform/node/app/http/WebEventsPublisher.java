package network.piranya.platform.node.app.http;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Handler;
import io.vertx.core.http.ServerWebSocket;
import network.piranya.platform.node.utilities.CollectionUtils;
import network.piranya.platform.node.utilities.Json;

public class WebEventsPublisher {
	
	public void publishEvent(WebEvent event) {
		try {
			String eventJson = Json.toJsonString(event);
			CollectionUtils.foreach(sockets().keySet(), socket -> socket.writeTextMessage(eventJson));
		} catch (Throwable ex) {
			LOG.warn(String.format("Failed to publish web event: %s", ex.getMessage()), ex);
		}
	}
	
	
	public Handler<ServerWebSocket> websocketHandler() {
		return socket -> {
			sockets().put(socket, true);
			socket.closeHandler(v -> sockets().remove(socket));
			socket.exceptionHandler(v -> sockets().remove(socket));
		};
	}
	
	
	private final ConcurrentMap<ServerWebSocket, Boolean> sockets = new ConcurrentHashMap<>();
	protected ConcurrentMap<ServerWebSocket, Boolean> sockets() {
		return sockets;
	}
	
	
	private static final Logger LOG = LoggerFactory.getLogger(WebEventsPublisher.class);
	
}
