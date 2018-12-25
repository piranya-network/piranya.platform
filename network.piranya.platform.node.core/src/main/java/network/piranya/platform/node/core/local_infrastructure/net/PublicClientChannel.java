package network.piranya.platform.node.core.local_infrastructure.net;

import network.piranya.infrastructure.pressing_udp.ChannelConfig;
import network.piranya.infrastructure.pressing_udp.PressingUdpClient;
import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.api.lang.ReplyHandler;
import network.piranya.platform.node.api.networking.nodes.Message;
import network.piranya.platform.node.api.networking.nodes.NodeContacts;
import network.piranya.platform.node.api.networking.nodes.PublicClientChannelRef;

public class PublicClientChannel implements PublicClientChannelRef {
	
	@Override
	public void send(Message message) {
		client().send(context().messagesEncoder().encode(message), true);
	}
	
	@Override
	public <Reply> ReplyHandler<Reply> talk(Message request, Class<Reply> replyType) {
		System.err.println("** talk: " + context().messagesEncoder().encode(request).buffer().length);
		return new ReplyHandlerAdapter<>(client().send(context().messagesEncoder().encode(request), true), context(), this::onLatency);
	}
	
	@Override
	public <Reply> ReplyHandler<Reply> talk(Message request, Class<Reply> replyType, int timeout) {
		return talk(request, replyType);
	}
	
	@Override
	public void close() throws Exception {
		unuse();
	}
	
	@Override
	public void unuse() {
		client().dispose();
	}
	
	public Optional<Integer> latency() {
		return latency;
	}
	
	
	protected void onLatency(long latency) {
		this.latency = Optional.of((int)latency);
	}
	private Optional<Integer> latency = Optional.empty();
	
	
	public PublicClientChannel(NodeContacts contacts, ChannelsContext channelsContext) {
		this.context = channelsContext;
		this.client = new PressingUdpClient(new ChannelConfig(contacts.host(), contacts.publicPort()).retryTimeout(200).maxRetries(5));
	}
	
	private final ChannelsContext context;
	protected ChannelsContext context() {
		return context;
	}
	
	private final PressingUdpClient client;
	protected PressingUdpClient client() {
		return client;
	}
	
}
