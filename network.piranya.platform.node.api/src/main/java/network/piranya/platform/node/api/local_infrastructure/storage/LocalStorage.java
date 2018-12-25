package network.piranya.platform.node.api.local_infrastructure.storage;

import java.util.List;

import network.piranya.platform.node.api.local_infrastructure.storage.specific.AccountsLocalDb;

public interface LocalStorage {
	
	PersistentQueue persistentQueue(String id, PersistentQueue.Config config, Object user);
	List<String> listPersistentQueues();
	void deletePersistentQueue(String id);
	
	KeyValueDb keyValueDb(String id, KeyValueDb.Config config, Object user);
	
	PropertiesStore properties(String id);
	
	
	AccountsLocalDb accountsLocalDb();
	
	KeyValueDb cacheDb();
	KeyValueDb nodeSettingsDb();
	
}
