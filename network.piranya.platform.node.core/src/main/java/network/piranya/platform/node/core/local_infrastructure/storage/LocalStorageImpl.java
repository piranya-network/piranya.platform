package network.piranya.platform.node.core.local_infrastructure.storage;

import static network.piranya.platform.node.utilities.CollectionUtils.*;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import network.piranya.platform.api.exceptions.PiranyaException;
import network.piranya.platform.node.api.booting.NetworkNodeConfig;
import network.piranya.platform.node.api.local_infrastructure.storage.KeyValueDb;
import network.piranya.platform.node.api.local_infrastructure.storage.LocalStorage;
import network.piranya.platform.node.api.local_infrastructure.storage.PersistentQueue;
import network.piranya.platform.node.api.local_infrastructure.storage.PropertiesStore;
import network.piranya.platform.node.api.local_infrastructure.storage.specific.AccountsLocalDb;
import network.piranya.platform.node.core.local_infrastructure.storage.specific.AccountsLocalDbImpl;
import network.piranya.platform.node.utilities.FileUtils;

public class LocalStorageImpl implements LocalStorage {
	
	@Override
	public synchronized PersistentQueue persistentQueue(String id, PersistentQueue.Config config, Object user) {
		ChronicleBasedPersistentQueue queueDb = queueDbs().get(id);
		if (queueDb == null) {
			queueDb = new ChronicleBasedPersistentQueue(queuesDir(), id, config, qdb -> disposeOfPersistentQueue(qdb));
			queueDbs().put(id, queueDb);
		}
		queueDb.use(user);
		return queueDb;
	}
	@Override
	public synchronized List<String> listPersistentQueues() {
		try {
			return Files.list(queuesDir().toPath()).map(p -> p.toFile().getName()).collect(Collectors.toList());
		} catch (Throwable ex) {
			throw new PiranyaException(ex);
		}
	}
	
	@Override
	public synchronized void deletePersistentQueue(String id) {
		if (queueDbs().containsKey(id)) {
			throw new PiranyaException(String.format("Can not delete persistent queue '%s' because it's in use currently", id));
		}
		
		FileUtils.deleteDirectory(new File(queuesDir(), id));
	}
	
	@Override
	public synchronized KeyValueDb keyValueDb(String id, KeyValueDb.Config config, Object user) {
		KeyValueDbImpl db = keyValueDbs().get(id);
		if (db == null) {
			db = new KeyValueDbImpl(id, config().dataDir(), config, this::disposeOfKeyValueDb);
			keyValueDbs().put(id, db);
		}
		db.use(user);
		return db;
	}
	
	@Override
	public PropertiesStore properties(String id) {
		return new PropertiesStore(new File(config().dataDir(), String.format("%s.properties", id)));
	}
	
	@Override
	public AccountsLocalDb accountsLocalDb() {
		return this.accountsLocalDb;
	}
	
	@Override
	public KeyValueDb cacheDb() {
		return this.cacheDb;
	}
	
	@Override
	public KeyValueDb nodeSettingsDb() {
		return this.cacheDb;
	}
	
	public void dispose() {
		foreach(keyValueDbs().values(), db -> db.dispose());
		foreach(queueDbs().values(), db -> db.dispose());
	}
	
	
	protected void disposeOfPersistentQueue(ChronicleBasedPersistentQueue queueDb) {
		queueDbs().remove(queueDb.id());
		queueDb.dispose();
	}
	
	protected void disposeOfKeyValueDb(KeyValueDbImpl kvDb) {
		keyValueDbs().remove(kvDb.id());
		kvDb.dispose();
	}
	
	
	public LocalStorageImpl(NetworkNodeConfig config) {
		this.config = config;
		this.accountsLocalDb = new AccountsLocalDbImpl(this);
		this.cacheDb = keyValueDb("CACHE", new KeyValueDb.Config(), this);
		this.queuesDir = new File(config.dataDir(), "queues");
		this.queuesDir.mkdirs();
	}
	
	private final NetworkNodeConfig config;
	protected NetworkNodeConfig config() { return config; }
	
	private final File queuesDir;
	protected File queuesDir() { return queuesDir; }
	
	private final Map<String, KeyValueDbImpl> keyValueDbs = new HashMap<>();
	protected Map<String, KeyValueDbImpl> keyValueDbs() { return keyValueDbs; }
	
	private final Map<String, ChronicleBasedPersistentQueue> queueDbs = new HashMap<>();
	protected Map<String, ChronicleBasedPersistentQueue> queueDbs() { return queueDbs; }
	
	private final AccountsLocalDb accountsLocalDb;
	private final KeyValueDb cacheDb;
	
}
