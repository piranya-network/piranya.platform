package network.piranya.platform.node.api.networking.nodes;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface PublicServiceChannelRef {
	
	<MessageType extends Message> void subscribe(Class<MessageType> messageType, Consumer<MessageType> listener);
	
	<Request extends Message, Reply extends Message> void subscribe(Class<Request> requestType, BiConsumer<Request, ReplySender<Reply>> listener);
	
	<MessageType extends Message> void subscribe(Class<MessageType> messageType, Predicate<MessageType> predicate, Consumer<MessageType> listener);
	
	<Request extends Message, Reply extends Message> void subscribe(Class<Request> requestType, Predicate<Request> predicate, BiConsumer<Request, ReplySender<Reply>> listener);
	
	void unuse();
	
}
