package network.piranya.platform.tests.commons.node;

import java.io.File;

import network.piranya.infrastructure.dcm4j.impl.jvm.PureJvmComponentModel;
import network.piranya.platform.api.accounting.NetworkCredentials;
import network.piranya.platform.api.accounting.NodeId;
import network.piranya.platform.node.api.booting.NetworkNodeConfig;
import network.piranya.platform.node.core.booting.NetworkNode;
import network.piranya.platform.node.core.local_infrastructure.LocalServicesProviderImpl;
import network.piranya.platform.tests.commons.utilities.FileUtils;

public class NodeBuilder {
	
	public NetworkNode build() {
		return new NetworkNode(credentials, config, new LocalServicesProviderImpl(config, credentials), new PureJvmComponentModel(), (eventType, data) -> {});
	}
	
	public NodeBuilder credentials(NetworkCredentials credentials) {
		this.credentials = credentials;
		return this;
	}
	private NetworkCredentials credentials;
	
	public NodeBuilder config(String nodeId, int baseNetworkPort, File dataDir) {
		this.config = new NetworkNodeConfig(new NodeId(nodeId, 0), baseNetworkPort, dataDir);
		return this;
	}
	public NodeBuilder config(String nodeId, int baseNetworkPort) {
		return config(nodeId, baseNetworkPort, FileUtils.createTempDir(true));
	}
	private NetworkNodeConfig config;
	
	
	public static NodeBuilder nodeBuilder() {
		return new NodeBuilder();
	}
	
}
