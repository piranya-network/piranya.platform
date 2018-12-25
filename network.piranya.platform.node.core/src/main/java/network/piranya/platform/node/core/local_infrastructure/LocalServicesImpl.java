package network.piranya.platform.node.core.local_infrastructure;

import network.piranya.platform.node.api.local_infrastructure.concurrency.Executor;

import static network.piranya.platform.node.utilities.CollectionUtils.foreach;

import java.util.ArrayList;
import java.util.List;

import network.piranya.platform.node.api.booting.NetworkNodeConfig;
import network.piranya.platform.node.api.local_infrastructure.LocalServices;
import network.piranya.platform.node.api.local_infrastructure.Log;
import network.piranya.platform.node.api.local_infrastructure.storage.LocalStorage;
import network.piranya.platform.node.api.networking.nodes.NodeChannelsProvider;
import network.piranya.platform.node.core.local_infrastructure.net.ChannelsContext;
import network.piranya.platform.node.core.local_infrastructure.net.NodeChannelsProviderImpl;

public class LocalServicesImpl implements LocalServices {
	
	@Override
	public NodeChannelsProvider channelsProvider() {
		return this.nodeChannelsProvider;
	}
	private NodeChannelsProviderImpl nodeChannelsProvider;
	
	@Override
	public Executor executor() {
		if (this.executor == null) {
			this.executor = new ExecutorImpl(new Executor.Config(1));
		}
		return this.executor;
	}
	private ExecutorImpl executor;
	
	@Override
	public Executor separateExecutor(Executor.Config config) {
		ExecutorImpl executor = new ExecutorImpl(config);
		separateExecutors.add(executor);
		return executor;
	}
	private final List<ExecutorImpl> separateExecutors = new ArrayList<>();
	
	@Override
	public LocalStorage localStorage() {
		return this.localStorage;
	}
	
	@Override
	public Log log() {
		if (this.log == null) {
			this.log = new LogImpl(owner);
		}
		return this.log;
	}
	private LogImpl log;
	
	@Override
	public void dispose() {
		if (this.executor != null) {
			this.executor.dispose();
		}
		if (this.nodeChannelsProvider != null) {
			this.nodeChannelsProvider.dispose();
		}
		foreach(separateExecutors, executor -> executor().dispose());
	}
	
	
	public LocalServicesImpl(Object owner, NetworkNodeConfig config, ChannelsContext channelsContext, LocalStorage localStorage) {
		this.owner = owner;
		this.config = config;
		this.channelsContext = channelsContext;
		this.localStorage = localStorage;
		this.nodeChannelsProvider = new NodeChannelsProviderImpl(channelsContext);
	}
	
	private final Object owner;
	
	private NetworkNodeConfig config;
	protected NetworkNodeConfig config() {
		return config;
	}
	
	private final ChannelsContext channelsContext;
	protected ChannelsContext channelsContext() {
		return channelsContext;
	}
	
	private final LocalStorage localStorage;
	
}
