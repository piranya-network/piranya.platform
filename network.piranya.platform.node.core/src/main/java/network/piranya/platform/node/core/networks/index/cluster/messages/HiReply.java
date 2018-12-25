package network.piranya.platform.node.core.networks.index.cluster.messages;

public class HiReply extends IndexClusterMessage {
	
	private final IndexClusterDetails indexClusterDetails;
	public IndexClusterDetails indexClusterDetails() {
		return indexClusterDetails;
	}
	
	public HiReply(IndexClusterDetails indexClusterDetails) {
		this.indexClusterDetails = indexClusterDetails;
	}
	
}
