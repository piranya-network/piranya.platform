package network.piranya.platform.node.api.booting;

import java.io.File;

import network.piranya.platform.api.accounting.NodeId;

public class NetworkNodeConfig {
	
	private final NodeId nodeId;
	public NodeId nodeId() {
		return nodeId;
	}
	
	private final int baseNetworkPort;
	public int baseNetworkPort() {
		return baseNetworkPort;
	}
	
	private final File nodeDir;
	public File nodeDir() {
		return nodeDir;
	}
	
	private final File dataDir;
	public File dataDir() {
		return dataDir;
	}
	
	public NetworkNodeConfig(NodeId nodeId, int baseNetworkPort, File nodeDir) {
		this.nodeId = nodeId;
		this.baseNetworkPort = baseNetworkPort;
		this.nodeDir = nodeDir;
		this.dataDir = new File(nodeDir, "data");
	}
	
	
	public int httpPort() {
		return baseNetworkPort() + 1;
	}
	
}
