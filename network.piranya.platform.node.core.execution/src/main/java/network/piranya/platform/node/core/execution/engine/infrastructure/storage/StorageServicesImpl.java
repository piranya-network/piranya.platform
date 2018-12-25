package network.piranya.platform.node.core.execution.engine.infrastructure.storage;

import static network.piranya.platform.node.utilities.CollectionUtils.foreach;

import java.util.HashMap;
import java.util.Map;

import network.piranya.platform.api.exceptions.PiranyaException;
import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.api.models.infrastructure.storage.QueueStoreConfig;
import network.piranya.platform.api.models.infrastructure.storage.FileStore;
import network.piranya.platform.api.models.infrastructure.storage.KeyValueStore;
import network.piranya.platform.api.models.infrastructure.storage.KeyValueStoreConfig;
import network.piranya.platform.api.models.infrastructure.storage.QueueStore;
import network.piranya.platform.api.models.infrastructure.storage.StorageServices;
import network.piranya.platform.node.core.execution.engine.bots.RunningBot;
import network.piranya.platform.node.utilities.DisposableSupport;

public class StorageServicesImpl implements StorageServices {
	
	@Override
	public QueueStore queue(String id, QueueStoreConfig config, Class<?>... entryTypes) {
		disposable.checkIfDisposed();
		validateIfProperUserBasedId(id);
		
		return queueFromBranch(storageOperator().queue(bot().map(b -> b.bot().ref()), id, config, entryTypes));
	}
	
	@Override
	public QueueStore queue(String id, Class<?>... entryTypes) {
		return queue(id, new QueueStoreConfig(false, false), entryTypes);
	}
	
	@Override
	public QueueStore globalQueue(String id, Class<?>... entryTypes) {
		return queue(id, new QueueStoreConfig(false, true), entryTypes);
	}
	
	@Override
	public void deleteGlobalQueue(String id) {
		disposable.checkIfDisposed();
		
		storageOperator().deleteGlobalQueue(id);
	}
	
	@Override
	public QueueStore useOutOfContext(QueueStore queue) {
		disposable.checkIfDisposed();
		
		return new QueueStoreImpl(((QueueStoreImpl)queue).branch());
	}
	
	
	@Override
	public KeyValueStore keyValue(String id, KeyValueStoreConfig config) {
		disposable.checkIfDisposed();
		validateIfProperUserBasedId(id);
		
		return kvFromBranch(storageOperator().keyValueStore(bot().map(b -> b.bot().ref()), id, config));
	}
	
	@Override
	public KeyValueStore keyValue(String id) {
		return keyValue(id, new KeyValueStoreConfig(false, false));
	}
	
	@Override
	public KeyValueStore globalKeyValue(String id) {
		return keyValue(id, new KeyValueStoreConfig(false, true));
	}
	
	@Override
	public KeyValueStore useOutOfContext(KeyValueStore keyValueStore) {
		return new KeyValueStoreImpl(((KeyValueStoreImpl)keyValueStore).branch());
	}
	
	@Override
	public FileStore files() {
		return fileStore;
	}
	
	
	protected QueueStoreImpl queueFromBranch(QueueStoreBranch branch) {
		QueueStoreImpl queue = queuesMap().get(branch.uniqueQueueId());
		if (queue == null) {
			queue = new QueueStoreImpl(branch);
			queuesMap().put(branch.uniqueQueueId(), queue);
		}
		return queue;
	}
	
	protected KeyValueStoreImpl kvFromBranch(KeyValueStoreBranch branch) {
		KeyValueStoreImpl kvStore = kvMap().get(branch.uniqueId());
		if (kvStore == null) {
			kvStore = new KeyValueStoreImpl(branch);
			kvMap().put(branch.uniqueId(), kvStore);
		}
		return kvStore;
	}
	
	public void dispose() {
		disposable.markDisposed();
		
		foreach(queuesMap().values(), queue -> queue.dispose());
		foreach(kvMap().values(), kv -> kv.dispose());
	}
	
	
	protected void validateIfProperUserBasedId(String id) {
		if (id.contains("#")) {
			throw new PiranyaException("Queue ID must not contain '#' character");
		}
	}
	
	
	public StorageServicesImpl(Optional<RunningBot> bot, StorageOperator storageOperator, String uniqueModuleId) {
		this.bot = bot;
		this.storageOperator = storageOperator;
		this.fileStore = new FileStoreImpl(storageOperator, uniqueModuleId);
	}
	
	private final Optional<RunningBot> bot;
	protected Optional<RunningBot> bot() { return bot; }
	
	private final StorageOperator storageOperator;
	protected StorageOperator storageOperator() { return storageOperator; }
	
	private final FileStoreImpl fileStore;
	
	private final Map<String, QueueStoreImpl> queuesMap = new HashMap<>();
	protected Map<String, QueueStoreImpl> queuesMap() { return queuesMap; }
	
	private final Map<String, KeyValueStoreImpl> kvMap = new HashMap<>();
	protected Map<String, KeyValueStoreImpl> kvMap() { return kvMap; }
	
	private DisposableSupport disposable = new DisposableSupport(this);
	
}
