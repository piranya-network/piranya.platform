package network.piranya.platform.api.models.infrastructure.storage;

public interface StorageServices {
	
	QueueStore queue(String id, QueueStoreConfig config, Class<?>... entryTypes);
	QueueStore queue(String id, Class<?>... entryTypes);
	QueueStore globalQueue(String id, Class<?>... entryTypes);
	void deleteGlobalQueue(String id);
	
	KeyValueStore keyValue(String id, KeyValueStoreConfig config);
	KeyValueStore keyValue(String id);
	KeyValueStore globalKeyValue(String id);
	
	QueueStore useOutOfContext(QueueStore queue);
	KeyValueStore useOutOfContext(KeyValueStore keyValueStore);
	
	FileStore files();
	
}
