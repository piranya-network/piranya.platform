package network.piranya.platform.node.api.networking.nodes;

public interface ConsumerChannel extends AutoCloseable {
	
	void send(Message request);
	
	ChunkSender sendChunk();
	
}
