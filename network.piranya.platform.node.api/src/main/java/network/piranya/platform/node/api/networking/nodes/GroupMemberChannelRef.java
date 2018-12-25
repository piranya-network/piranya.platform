package network.piranya.platform.node.api.networking.nodes;

import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.api.lang.ReplyHandler;

public interface GroupMemberChannelRef extends AutoCloseable {
	
	void send(Message request);
	
	<Reply> ReplyHandler<Reply> talk(Message request, Class<Reply> replyType);
	
	<Reply> ReplyHandler<Reply> talk(Message request, Class<Reply> replyType, int timeout);
	
	//<MessageType extends Message> void subscribe(Class<MessageType> messageType, Consumer<MessageType> listener);
	
	ChunkSender sendChunk();
	
	Optional<Integer> latency();
	
	void unuse();
	
}
