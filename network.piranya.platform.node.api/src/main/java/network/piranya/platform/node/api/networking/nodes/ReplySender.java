package network.piranya.platform.node.api.networking.nodes;

public interface ReplySender<Reply> {
	
	void sendReply(Reply reply);
	
	void sendError(ErrorMessage errorMessage);
	
	ChunkSender sendChunk();
	
}