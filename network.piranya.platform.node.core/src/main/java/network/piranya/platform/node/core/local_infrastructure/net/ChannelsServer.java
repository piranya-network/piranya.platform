package network.piranya.platform.node.core.local_infrastructure.net;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import network.piranya.infrastructure.pressing_udp.ChannelConfig;
import network.piranya.infrastructure.pressing_udp.PressingUdpServer;
import network.piranya.platform.node.api.booting.NetworkNodeConfig;
import network.piranya.platform.node.api.networking.nodes.Message;
import network.piranya.platform.node.api.networking.nodes.ReplySender;

public class ChannelsServer {
	
	public Subscription subscribe(Object subscriber, Class<Message> messageType, Predicate<Message> predicate, Consumer<Message> listener) {
		return subscribe(subscriber, messageType, predicate, (message, replySender) -> listener.accept(message));
	}
	
	public Subscription subscribe(Object subscriber, Class<Message> requestType, Predicate<Message> predicate, BiConsumer<Message, ReplySender<Message>> listener) {
		Subscription subscription = new Subscription(requestType, predicate, listener);
		subscriptions().put(subscription, true);
		return subscription;
	}
	
	public void unsubscribe(Object subscriber) {
		List<Subscription> subscriptionToRemove = new ArrayList<>();
		for (Subscription subscription : subscriptions().keySet()) {
			if (subscription == subscriber) {
				subscriptionToRemove.add(subscription);
			}
		}
		for (Subscription subscription : subscriptionToRemove) {
			subscriptions().remove(subscription);
		}
	}
	
	public void dispose() {
		server().dispose();
	}
	
	
	protected void onMessage(network.piranya.infrastructure.pressing_udp.Message puMessage, network.piranya.infrastructure.pressing_udp.utilities.ReplySender replySender) {
		try {
			// TODO threading
			Message message = parseMessage(puMessage);
			System.out.println("message: " + message);
			for (Subscription subscription : subscriptions().keySet()) {
				if (subscription.requestType().isInstance(message) && subscription.predicate().test(message)) {
					subscription.listener().accept(message, new ReplySenderImpl<>(replySender, messagesEncoder()));
					return; /// message can be handled only by one subscriber
				}
			}
		} catch (Throwable ex) {
			// log
			ex.printStackTrace();
		}
	}
	
	protected Message parseMessage(network.piranya.infrastructure.pressing_udp.Message message) {
		return messagesEncoder().decode(message);
	}
	
	
	public ChannelsServer(NetworkNodeConfig config, MessagesEncoder messagesEncoder) {
		this.config = config;
		this.messagesEncoder = messagesEncoder;
		
		this.server = new PressingUdpServer(new ChannelConfig("0.0.0.0", config.baseNetworkPort()).retryTimeout(200).maxRetries(5));
		server().onMessage(this::onMessage);
	}
	
	private final NetworkNodeConfig config;
	protected NetworkNodeConfig getConfig() {
		return config;
	}
	
	private final PressingUdpServer server;
	protected PressingUdpServer server() {
		return server;
	}
	
	private final ConcurrentMap<Subscription, Boolean> subscriptions = new ConcurrentHashMap<>();
	protected ConcurrentMap<Subscription, Boolean> subscriptions() {
		return subscriptions;
	}
	
	private final MessagesEncoder messagesEncoder;
	protected MessagesEncoder messagesEncoder() {
		return messagesEncoder;
	}
	
	
	public static class Subscription {
		
		private final Class<Message> requestType;
		public Class<Message> requestType() {
			return requestType;
		}
		
		private final Predicate<Message> predicate;
		public Predicate<Message> predicate() {
			return predicate;
		}
		
		private final BiConsumer<Message, ReplySender<Message>> listener;
		public BiConsumer<Message, ReplySender<Message>> listener() {
			return listener;
		}
		
		public Subscription(Class<Message> requestType, Predicate<Message> predicate, BiConsumer<Message, ReplySender<Message>> listener) {
			this.requestType = requestType;
			this.predicate = predicate;
			this.listener = listener;
		}
	}
	
}
