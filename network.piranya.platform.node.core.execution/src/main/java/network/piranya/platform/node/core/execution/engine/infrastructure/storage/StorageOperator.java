package network.piranya.platform.node.core.execution.engine.infrastructure.storage;

import static network.piranya.platform.node.utilities.CollectionUtils.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import network.piranya.platform.api.exceptions.PiranyaException;
import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.api.models.bots.BotRef;
import network.piranya.platform.api.models.infrastructure.storage.KeyValueStoreConfig;
import network.piranya.platform.api.models.infrastructure.storage.QueueStoreConfig;
import network.piranya.platform.node.api.local_infrastructure.storage.KeyValueDb;
import network.piranya.platform.node.api.local_infrastructure.storage.LocalStorage;
import network.piranya.platform.node.api.local_infrastructure.storage.PersistentQueue;
import network.piranya.platform.node.utilities.Encoder;

public class StorageOperator {
	
	public QueueStoreBranch queue(Optional<BotRef> botRef, String id, QueueStoreConfig config, Class<?>... entryTypes) {
		if (id.contains("#")) {//(!ID_PATTERN.matcher(id).matches()) {
			throw new IllegalArgumentException(String.format("Invalid Queue ID format '%s'", id));
		}
		if (!config.isGlobal() && !botRef.isPresent()) {
			throw new PiranyaException("Non-global queues can not be used out of bot's context");
		}
		
		String uniqueQueueId = queueKey(botRef.orElse(null), id, config.isGlobal());
		if (!queueBranches().containsKey(uniqueQueueId)) {
			QueueStoreBranch branch = new QueueStoreBranch(uniqueQueueId, localStorage().persistentQueue(uniqueQueueId, new PersistentQueue.Config(config.requireExistence()), this),
					config.maxMessageSize(), createEncoder(entryTypes), this::releaseBranch);
			
			if (isReplaying()) {
				branch.startReplay();
			}
			
			queueBranches().put(uniqueQueueId, branch);
			return branch;
		} else {
			return queueBranches().get(uniqueQueueId);
		}
	}
	
	public KeyValueStoreBranch keyValueStore(Optional<BotRef> botRef, String id, KeyValueStoreConfig config) {
		if (!ID_PATTERN.matcher(id).matches()) {
			throw new IllegalArgumentException(String.format("Invalid Key Value Store ID format '%s'", id));
		}
		if (!config.isGlobal() && !botRef.isPresent()) {
			throw new PiranyaException("Non-global key value stores can not be used out of bot's context");
		}
		
		String key = kvKey(botRef.orElse(null), id, config.isGlobal());
		if (!kvBranches().containsKey(key)) {
			KeyValueStoreBranch branch = new KeyValueStoreBranch(key, keyValueDb());
			
			if (isReplaying()) {
				branch.startReplay();
			}
			
			kvBranches().put(key, branch);
			return branch;
		} else {
			return kvBranches().get(key);
		}
	}
	
	public void deleteGlobalQueue(String id) {
		localStorage().deletePersistentQueue(queueKey(null, id, true));
	}
	
	protected void releaseBranch(QueueStoreBranch branch) {
		queueBranches().remove(branch.uniqueQueueId());
		branch.persistentueue().release(this);
	}
	
	
	public void finishReplay() {
		setReplaying(false);
		
		foreach(queueBranches().values(), branch -> branch.finishReplay());
	}
	
	
	public String queueKey(BotRef botRef, String id, boolean isGlobal) {
		String queueId = isGlobal ? String.format("global#%s", id) : String.format("%s#%s", botRef.botId(), id);
		queueId = queueId.replaceAll("(\\:|\\>|\\||\\*|\\/)", "~");
		return queueId;
	}
	
	public String kvKey(BotRef botRef, String id, boolean isGlobal) {
		return isGlobal ? String.format("global#%s", id) : String.format("%s#%s", botRef.botId(), id);
	}
	
	protected Encoder createEncoder(Class<?>... entryTypes) {
		Encoder encoder = new Encoder();
		short typeId = 0;
		for (Class<?> entryType : entryTypes) {
			encoder.registerDataType(entryType, ++typeId);
		}
		return encoder;
	}
	
	
	public StorageOperator(LocalStorage localStorage) {
		this.localStorage = localStorage;
		
		this.keyValueDb =  localStorage.keyValueDb("bots-kv-data", new KeyValueDb.Config(), this);
	}
	
	private final LocalStorage localStorage;
	protected LocalStorage localStorage() { return localStorage; }
	
	private final Map<String, QueueStoreBranch> queueBranches = new HashMap<>();
	protected Map<String, QueueStoreBranch> queueBranches() { return queueBranches; }
	
	private final Map<String, KeyValueStoreBranch> kvBranches = new HashMap<>();
	protected Map<String, KeyValueStoreBranch> kvBranches() { return kvBranches; }
	
	private final KeyValueDb keyValueDb;
	protected KeyValueDb keyValueDb() { return keyValueDb; }
	
	private boolean isReplaying = false;
	protected boolean isReplaying() { return isReplaying; }
	protected void setReplaying(boolean isReplaying) { this.isReplaying = isReplaying; }
	
	public void startReplay() {
		setReplaying(true);
	}
	
	
	private static final String QUEUE_ID_REGEX = "^[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38}$";
	private static final Pattern ID_PATTERN = Pattern.compile(QUEUE_ID_REGEX);
	
}
