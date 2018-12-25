package network.piranya.platform.node.core.networks.index.cluster.messages;

import java.util.List;

import network.piranya.platform.node.api.networking.nodes.NodeContacts;

public class IndexClusterDetails {
	
	private final List<NodeContacts> nodes;
	public List<NodeContacts> nodes() {
		return nodes;
	}
	
	public IndexClusterDetails(List<NodeContacts> nodes) {
		this.nodes = nodes;
	}
}