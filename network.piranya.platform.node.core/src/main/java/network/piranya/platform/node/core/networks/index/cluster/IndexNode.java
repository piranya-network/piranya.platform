package network.piranya.platform.node.core.networks.index.cluster;

import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.api.lang.ReplyHandler;
import network.piranya.platform.node.accounts.Account;
import network.piranya.platform.node.api.networking.nodes.GroupMemberChannelRef;
import network.piranya.platform.node.api.networking.nodes.NodeChannelsProvider;
import network.piranya.platform.node.api.networking.nodes.NodeChannelsProvider.ChannelGroupType;
import network.piranya.platform.node.core.networks.index.cluster.messages.Hi;
import network.piranya.platform.node.core.networks.index.cluster.messages.HiReply;
import network.piranya.platform.node.core.networks.index.cluster.messages.JoinClusterRequest;
import network.piranya.platform.node.utilities.TimeService;

public class IndexNode {
	
	public ReplyHandler<HiReply> hi() {
		return channel().talk(new Hi(), HiReply.class);
	}
	
	public void joinCluster() {
		channel().send(new JoinClusterRequest());
	}
	
	
	public IndexNode(Account account, NodeChannelsProvider channels) {
		this.account = account;
		this.channels = channels;
		
		this.channel = channels.accessGroupMemberChannel(account.activity().mainAccessPoint().nodeContacts(), ChannelGroupType.INDEX_CLUSTER, 0);
		
		updateHeartbeat();
	}
	
	private final Account account;
	public Account account() {
		return account;
	}
	
	public Optional<Integer> latency() {
		return channel().latency();
	}
	
	private final NodeChannelsProvider channels;
	protected NodeChannelsProvider channels() {
		return channels;
	}
	
	private final GroupMemberChannelRef channel;
	public GroupMemberChannelRef channel() {
		return channel;
	}
	
	public void updateHeartbeat() {
		this.lastHeartbeatTime = TimeService.now();
	}
	public long lastHeartbeatTime() {
		return this.lastHeartbeatTime;
	}
	private long lastHeartbeatTime = 0L;
	
}
