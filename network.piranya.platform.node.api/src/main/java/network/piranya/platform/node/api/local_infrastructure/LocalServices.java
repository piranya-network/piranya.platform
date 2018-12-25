package network.piranya.platform.node.api.local_infrastructure;

import network.piranya.platform.node.api.local_infrastructure.concurrency.Executor;
import network.piranya.platform.node.api.local_infrastructure.storage.LocalStorage;
import network.piranya.platform.node.api.networking.nodes.NodeChannelsProvider;

public interface LocalServices {
	
	NodeChannelsProvider channelsProvider();
	
	Executor executor();
	
	Executor separateExecutor(Executor.Config config);
	
	LocalStorage localStorage();
	
	Log log();
	
	
	void dispose();
	
}