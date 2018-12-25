package network.piranya.platform.node.api.networking.nodes;

import java.util.function.Predicate;

public interface NodeChannelsProvider {
	
	GroupMemberChannelRef accessGroupMemberChannel(NodeContacts memberContacts, ChannelGroupType groupType, int groupId);
	
	<BaseMessageType extends Message> GroupChannelRef useGroupChannel(
			ChannelGroupType groupType, int groupId, Class<BaseMessageType> baseMessageType, Predicate<BaseMessageType> predicate);
	
	PublicServiceChannelRef usePublicServiceChannel();
	
	PublicClientChannelRef accessClientChannel(NodeContacts contacts);
	
	//ConsumerChannel accessConsumerChannel(NodeContacts contacts);
	
	
	public static enum ChannelGroupType { INDEX_CLUSTER, ACCOUNTING_CLUSTER }
	
}
