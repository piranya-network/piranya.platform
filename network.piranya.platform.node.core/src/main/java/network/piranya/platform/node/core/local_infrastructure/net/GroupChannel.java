package network.piranya.platform.node.core.local_infrastructure.net;

import static network.piranya.platform.node.utilities.CollectionUtils.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

import network.piranya.platform.node.api.networking.nodes.ChunkSender;
import network.piranya.platform.node.api.networking.nodes.GroupChannelRef;
import network.piranya.platform.node.api.networking.nodes.Message;
import network.piranya.platform.node.api.networking.nodes.NodeContacts;

public class GroupChannel implements GroupChannelRef {
	
	@Override
	public void publish(Message message) {
		foreach(members().values(), member -> member.send(message));
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <MessageType extends Message> void subscribe(Class<MessageType> messageType, Consumer<MessageType> listener) {
		context().server().subscribe(this, (Class<Message>)messageType, message -> baseMessageType().isInstance(message) && predicate().test(message), (Consumer<Message>)listener);
	}
	
	@Override
	public ChunkSender publishChunk() {
		return new ChunkSenderImpl(message -> publish(message), context());
	}
	
	@Override
	public void unuse() {
		context().server().unsubscribe(this);
		foreach(members().values(), member -> member.unuse());
	}
	
	
	public GroupMemberChannel memberChannel(NodeContacts memberContacts) {
		GroupMemberChannel memberChannel = members().get(memberContacts);
		if (memberChannel == null) {
			memberChannel = new GroupMemberChannel(memberContacts, context(), this);
			members().put(memberContacts, memberChannel);
			memberChannel.use();
		}
		memberChannel.use();
		return memberChannel;
	}
	
	public void remove(GroupMemberChannel memberChannel) {
		members().remove(memberChannel.contacts());
	}
	
	
	public GroupChannel(ChannelsContext context, Class<Message> baseMessageType, Predicate<Message> predicate) {
		this.context = context;
		this.baseMessageType = baseMessageType;
		this.predicate = predicate;
	}
	
	private final ChannelsContext context;
	protected ChannelsContext context() {
		return context;
	}
	
	private final Class<Message> baseMessageType;
	protected Class<Message> baseMessageType() {
		return baseMessageType;
	}
	
	private final Predicate<Message> predicate;
	protected Predicate<Message> predicate() {
		return predicate;
	}
	
	private final ConcurrentMap<NodeContacts, GroupMemberChannel> members = new ConcurrentHashMap<>();
	protected ConcurrentMap<NodeContacts, GroupMemberChannel> members() {
		return members;
	}
	
}
