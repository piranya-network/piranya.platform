package network.piranya.platform.node.core.networks.index.cluster.messages;

import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.node.accounts.Account;


public class FollowupEntryMessage extends IndexClusterMessage {
	
	private final Optional<Account> account;
	public Optional<Account> account() {
		return account;
	}
	
	private final boolean isFollowupComplete;
	public boolean isFollowupComplete() {
		return isFollowupComplete;
	}
	
	public FollowupEntryMessage(Account account) {
		this.account = Optional.of(account);
		this.isFollowupComplete = false;
	}
	
	public FollowupEntryMessage(boolean isFollowupComplete) {
		this.isFollowupComplete = isFollowupComplete;
		this.account = Optional.empty();
	}
	
}
