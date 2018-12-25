package network.piranya.platform.node.core.local_infrastructure.net;

import java.util.concurrent.atomic.AtomicInteger;

import network.piranya.platform.node.api.networking.nodes.ChunkSender;
import network.piranya.platform.node.api.networking.nodes.GroupMemberChannelRef;
import network.piranya.platform.node.api.networking.nodes.NodeContacts;

public class GroupMemberChannel extends PublicClientChannel implements GroupMemberChannelRef {
	
	@Override
	public ChunkSender sendChunk() {
		return new ChunkSenderImpl(message -> send(message), context());
	}
	
	@Override
	public void unuse() {
		if (usersCount.decrementAndGet() == 0) {
			super.unuse();
			group().remove(this);
		}
	}
	
	
	public void use() {
		usersCount.incrementAndGet();
	}
	
	
	public GroupMemberChannel(NodeContacts contacts, ChannelsContext channelsContext, GroupChannel group) {
		super(contacts, channelsContext);
		this.contacts = contacts;
		this.group = group;
	}
	
	private final GroupChannel group;
	protected GroupChannel group() {
		return group;
	}
	
	private final NodeContacts contacts;
	public NodeContacts contacts() {
		return contacts;
	}
	
	private final AtomicInteger usersCount = new AtomicInteger(0);
	
}
