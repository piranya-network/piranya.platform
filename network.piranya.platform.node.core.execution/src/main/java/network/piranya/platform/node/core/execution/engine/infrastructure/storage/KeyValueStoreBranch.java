package network.piranya.platform.node.core.execution.engine.infrastructure.storage;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;

import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.node.api.local_infrastructure.storage.KeyValueDb;

public class KeyValueStoreBranch {
	
	public void put(String key, String value) {
		if (!isReplaying()) {
			db().put(actualKey(key), valueBuffer(value));
		} else {
			// 
		}
	}
	
	public Optional<String> get(String key) {
		return db().get(actualKey(key)).map(valueBuffer -> bufferToString(valueBuffer));
	}
	
	public void delete(String key) {
		db().delete(actualKey(key));
	}
	
	public void iterate(String keyPrefix, BiConsumer<String, String> consumer) {
		db().iterate(actualKeyBytes(keyPrefix), (keyBuffer, valueBuffer) -> {
			String fullKey = bufferToString(keyBuffer);
			consumer.accept(fullKey.substring(fullKey.lastIndexOf('#') + 1), bufferToString(valueBuffer));
		});
	}
	
	
	public void finishReplay() {
		setReplaying(false);
		
		catchup();
	}
	
	protected void catchup() {
		// first migrate to queue based writing since LMDB is slow at writing, then do catchup logic after factoring in queue-based writing.
	}
	
	
	protected ByteBuffer valueBuffer(String value) {
		byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
		ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
		return buffer.put(bytes).flip();
	}
	
	protected ByteBuffer actualKey(String key) {
		byte[] bytes = actualKeyBytes(key);
		ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
		return buffer.put(bytes).flip();
	}
	
	protected byte[] actualKeyBytes(String key) {
		return String.format("%s#%s", uniqueId(), key).getBytes(StandardCharsets.UTF_8);
	}
	protected String bufferToString(ByteBuffer buffer) {
		return StandardCharsets.UTF_8.decode(buffer).toString();
	}
	
	
	public KeyValueStoreBranch(String uniqueId, KeyValueDb db) {
		this.uniqueId = uniqueId;
		this.db = db;
	}
	
	private final KeyValueDb db;
	protected KeyValueDb db() { return db; }
	
	private final String uniqueId;
	public String uniqueId() { return uniqueId; }
	
	
	private boolean isReplaying = false;
	protected boolean isReplaying() { return isReplaying; }
	protected void setReplaying(boolean isReplaying) { this.isReplaying = isReplaying; }
	
	public void startReplay() {
		setReplaying(true);
	}
	
}
