package network.piranya.platform.node.core.local_infrastructure.net;

import java.util.ArrayList;
import java.util.List;

import network.piranya.platform.node.api.networking.nodes.Message;
import network.piranya.platform.node.core.networks.index.cluster.messages.FollowupEntryMessage;
import network.piranya.platform.api.exceptions.PiranyaException;

public class ChunkMessage extends Message {
	
	public void addMessage(Message message) {
		if (message instanceof FollowupEntryMessage) {
			followupEntryMessages().add((FollowupEntryMessage)message);
		} else {
			throw new PiranyaException(String.format("Message %s not suppored for chunking", message.getClass().getName()));
		}
	}
	
	public List<Message> getMessages() {
		List<Message> result = new ArrayList<>();
		result.addAll(followupEntryMessages());
		return result;
	}
	
	
	private final List<FollowupEntryMessage> followupEntryMessages = new ArrayList<>();
	protected List<FollowupEntryMessage> followupEntryMessages() {
		return followupEntryMessages;
	}
	
}
