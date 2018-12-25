package network.piranya.platform.node.core.local_infrastructure.net.utilities;

import java.util.function.Consumer;

import network.piranya.platform.node.api.networking.nodes.ChunkSender;
import network.piranya.platform.node.api.networking.nodes.Message;
import network.piranya.platform.node.core.local_infrastructure.net.ChannelsContext;

public class ChannelsGroup {
	
	public void publish(Message message) {
		
	}
	
	public <MessageType extends Message> void subscribe(Class<MessageType> messageType, Consumer<MessageType> listener) {
		
	}
	
	public ChunkSender publishChunk() {
		return null;
	}
	
	public void dispose() {
	}
	
	
	public ChannelsGroup(ChannelsContext channelsContext) {
		this.context = channelsContext;
	}
	
	private final ChannelsContext context;
	protected ChannelsContext context() {
		return context;
	}
	
}
