package network.piranya.platform.node.core.local_infrastructure.net;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import network.piranya.platform.node.api.networking.nodes.Message;
import network.piranya.platform.node.api.networking.nodes.PublicServiceChannelRef;
import network.piranya.platform.node.api.networking.nodes.ReplySender;

public class PublicServiceChannel implements PublicServiceChannelRef {
	
	@Override
	public <MessageType extends Message> void subscribe(Class<MessageType> messageType, Consumer<MessageType> listener) {
		subscribe(messageType, message -> true, listener);
	}
	
	@Override
	public <Request extends Message, Reply extends Message> void subscribe(Class<Request> requestType, BiConsumer<Request, ReplySender<Reply>> listener) {
		subscribe(requestType, request -> true, listener);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <MessageType extends Message> void subscribe(Class<MessageType> messageType, Predicate<MessageType> predicate, Consumer<MessageType> listener) {
		context().server().subscribe(this, (Class<Message>)messageType, (Predicate<Message>)predicate, (Consumer<Message>)listener);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <Request extends Message, Reply extends Message> void subscribe(Class<Request> requestType, Predicate<Request> predicate, BiConsumer<Request, ReplySender<Reply>> listener) {
		context().server().subscribe(this, (Class<Message>)requestType, (Predicate<Message>)predicate,
				(BiConsumer<Message, ReplySender<Message>>)((BiConsumer<?, ?>)listener));
	}
	
	@Override
	public void unuse() {
		context().server().unsubscribe(this);
	}
	
	
	public PublicServiceChannel(ChannelsContext channelsContext) {
		this.context = channelsContext;
	}
	
	private final ChannelsContext context;
	protected ChannelsContext context() {
		return context;
	}
	
}
