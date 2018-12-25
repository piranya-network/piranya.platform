package network.piranya.platform.node.api.networking.nodes;

import java.util.function.Consumer;

public interface GroupChannelRef {
	
	void publish(Message message);
	
	<MessageType extends Message> void subscribe(Class<MessageType> messageType, Consumer<MessageType> listener);
	
	ChunkSender publishChunk();
	
	void unuse();
	
}
