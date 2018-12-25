package network.piranya.platform.node.core.networks.index.cluster.messages;

public class FollowupRequest extends IndexClusterMessage {
	
	public FollowupRequest(long lastAccountsUpdateTime) {
		this.lastAccountsUpdateTime = lastAccountsUpdateTime;
	}
	
	private final long lastAccountsUpdateTime;
	public long lastAccountsUpdateTime() {
		return lastAccountsUpdateTime;
	}
	
}
