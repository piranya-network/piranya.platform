package network.piranya.platform.node.core.local_infrastructure;

import network.piranya.platform.node.api.local_infrastructure.LocalServices;
import network.piranya.platform.node.api.local_infrastructure.LocalServicesProvider;
import network.piranya.platform.node.api.local_infrastructure.concurrency.Executor;
import network.piranya.platform.api.accounting.NetworkCredentials;
import network.piranya.platform.node.api.booting.NetworkNodeConfig;
import network.piranya.platform.node.core.local_infrastructure.net.ChannelsContext;
import network.piranya.platform.node.core.local_infrastructure.storage.LocalStorageImpl;

public class LocalServicesProviderImpl implements LocalServicesProvider {
	
	@Override
	public LocalServices services(Object owner) {
		return new LocalServicesImpl(owner, config(), channelsContext(), localStorage());
	}
	
	public void dispose() {
		channelsContext().dispose();
		localStorage().dispose();
	}
	
	
	public LocalServicesProviderImpl(NetworkNodeConfig config, NetworkCredentials credentials) {
		this.config = config;
		this.localStorage = new LocalStorageImpl(config);
		this.channelsContext = new ChannelsContext(credentials, config, new ExecutorImpl(new Executor.Config(1)), localStorage().accountsLocalDb());
	}
	
	private NetworkNodeConfig config;
	protected NetworkNodeConfig config() {
		return config;
	}
	
	private final ChannelsContext channelsContext;
	protected ChannelsContext channelsContext() {
		return channelsContext;
	}
	
	private final LocalStorageImpl localStorage;
	protected LocalStorageImpl localStorage() {
		return localStorage;
	}
	
}
