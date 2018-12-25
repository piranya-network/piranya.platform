package network.piranya.platform.node.api.networking.nodes;

import network.piranya.platform.api.lang.ReplyHandler;

public interface PublicClientChannelRef extends AutoCloseable {
	
	void send(Message request);
	
	<Reply> ReplyHandler<Reply> talk(Message request, Class<Reply> replyType);
	
	<Reply> ReplyHandler<Reply> talk(Message request, Class<Reply> replyType, int timeout);
	
	void unuse();
	
}
