package network.piranya.platform.node.core.local_infrastructure.net;

import static network.piranya.platform.node.utilities.CollectionUtils.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;

import network.piranya.platform.api.exceptions.PiranyaException;
import network.piranya.platform.node.api.networking.nodes.GroupChannelRef;
import network.piranya.platform.node.api.networking.nodes.GroupMemberChannelRef;
import network.piranya.platform.node.api.networking.nodes.Message;
import network.piranya.platform.node.api.networking.nodes.NodeChannelsProvider;
import network.piranya.platform.node.api.networking.nodes.NodeContacts;
import network.piranya.platform.node.api.networking.nodes.PublicClientChannelRef;
import network.piranya.platform.node.api.networking.nodes.PublicServiceChannelRef;

public class NodeChannelsProviderImpl implements NodeChannelsProvider {
	
	@Override
	@SuppressWarnings("unchecked")
	public <BaseMessageType extends Message> GroupChannelRef useGroupChannel(ChannelGroupType groupType,
			int groupId, Class<BaseMessageType> baseMessageType, Predicate<BaseMessageType> predicate) {
		GroupChannel groupChannel = groupsChannels().get(groupKey(groupType, groupId));
		if (groupChannel == null) {
			groupChannel = new GroupChannel(context(), (Class<Message>)baseMessageType, (Predicate<Message>)predicate);
			groupsChannels().put(groupKey(groupType, groupId), groupChannel);
		}
		return groupChannel;
	}
	
	@Override
	public GroupMemberChannelRef accessGroupMemberChannel(NodeContacts memberContacts, ChannelGroupType groupType, int groupId) {
		GroupChannel groupChannel = groupsChannels().get(groupKey(groupType, groupId));
		if (groupChannel == null) {
			throw new PiranyaException(String.format("Group Channel '%s' is not registered yet. Invoke 'useGroupChannel' method first.", groupKey(groupType, groupId)));
		}
		return groupChannel.memberChannel(memberContacts);
	}
	
	@Override
	public PublicServiceChannelRef usePublicServiceChannel() {
		if (this.publicServiceChannel == null) {
			this.publicServiceChannel = new PublicServiceChannel(context());
		}
		return this.publicServiceChannel;
	}
	
	@Override
	public PublicClientChannelRef accessClientChannel(NodeContacts contacts) {
		PublicClientChannel publicClientChannel = publicClientChannels().get(contacts);
		if (publicClientChannel == null) {
			publicClientChannel = new PublicClientChannel(contacts, context());
			publicClientChannels().put(contacts, publicClientChannel);
		}
		return publicClientChannel;
	}
	
	public void dispose() {
		foreach(groupsChannels().values(), channel -> channel.unuse());
		foreach(publicClientChannels().values(), channel -> channel.unuse());
		if (this.publicServiceChannel != null) this.publicServiceChannel.unuse();
	}
	
	
	protected String groupKey(ChannelGroupType groupType, int groupId) {
		return String.format("%s:%s", groupType, groupId);
	}
	
	
	public NodeChannelsProviderImpl(ChannelsContext channelsContext) {
		this.context = channelsContext;
	}
	
	private final ChannelsContext context;
	protected ChannelsContext context() {
		return context;
	}
	
	private final ConcurrentMap<String, GroupChannel> groupsChannels = new ConcurrentHashMap<>();
	protected ConcurrentMap<String, GroupChannel> groupsChannels() {
		return groupsChannels;
	}
	
	private final ConcurrentMap<NodeContacts, PublicClientChannel> publicClientChannels = new ConcurrentHashMap<>();
	protected ConcurrentMap<NodeContacts, PublicClientChannel> publicClientChannels() {
		return publicClientChannels;
	}
	
	private PublicServiceChannel publicServiceChannel;
	
}
