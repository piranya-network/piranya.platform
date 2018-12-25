package network.piranya.platform.node.core.local_infrastructure.net;

import network.piranya.platform.node.api.networking.nodes.ChunkSender;
import network.piranya.platform.node.api.networking.nodes.ErrorMessage;
import network.piranya.platform.node.api.networking.nodes.Message;
import network.piranya.platform.node.api.networking.nodes.ReplySender;

public class ReplySenderImpl<Reply> implements ReplySender<Reply> {
	
	@Override
	public void sendReply(Reply reply) {
		sender().sendReply(messagesEncoder().encode((Message)reply));
	}
	
	@Override
	public void sendError(ErrorMessage errorMessage) {
		throw new RuntimeException("NotImplemented");
	}
	
	@Override
	public ChunkSender sendChunk() {
		throw new RuntimeException("NotImplemented");
	}
	
	
	public ReplySenderImpl(network.piranya.infrastructure.pressing_udp.utilities.ReplySender sender, MessagesEncoder messagesEncoder) {
		this.sender = sender;
		this.messagesEncoder = messagesEncoder;
	}
	
	private final network.piranya.infrastructure.pressing_udp.utilities.ReplySender sender;
	protected network.piranya.infrastructure.pressing_udp.utilities.ReplySender sender() {
		return sender;
	}
	
	private final MessagesEncoder messagesEncoder;
	protected MessagesEncoder messagesEncoder() {
		return messagesEncoder;
	}
	
}
