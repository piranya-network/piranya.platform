package network.piranya.platform.api.models.infrastructure.storage;

public interface QueueStore extends QueueStoreReader {
	
	void append(Object entry);
	
}
