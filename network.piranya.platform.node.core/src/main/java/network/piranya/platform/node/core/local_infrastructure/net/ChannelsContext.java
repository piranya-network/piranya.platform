package network.piranya.platform.node.core.local_infrastructure.net;

import network.piranya.platform.api.accounting.NetworkCredentials;
import network.piranya.platform.node.api.booting.NetworkNodeConfig;
import network.piranya.platform.node.api.local_infrastructure.concurrency.Executor;
import network.piranya.platform.node.api.local_infrastructure.storage.specific.AccountsLocalDb;
import network.piranya.platform.node.core.local_infrastructure.security.MessageSecurityManager;

public class ChannelsContext {
	
	public void dispose() {
		executionManager().dispose();
		server().dispose();
	}
	
	
	public ChannelsContext(NetworkCredentials credentials, NetworkNodeConfig config, Executor executionManager, AccountsLocalDb accountsLocalDb) {
		this.executionManager = executionManager;
		this.messagesEncoder = new MessagesEncoder(new MessageSecurityManager(credentials, accountsLocalDb));
		this.server = new ChannelsServer(config, messagesEncoder());
	}
	
	private final ChannelsServer server;
	public ChannelsServer server() {
		return server;
	}
	
	private final Executor executionManager;
	public Executor executionManager() {
		return executionManager;
	}
	
	private final MessagesEncoder messagesEncoder;
	public MessagesEncoder messagesEncoder() {
		return messagesEncoder;
	}
	
}
