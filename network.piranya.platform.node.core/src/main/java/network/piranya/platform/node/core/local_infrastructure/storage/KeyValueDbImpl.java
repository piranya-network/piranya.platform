package network.piranya.platform.node.core.local_infrastructure.storage;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.lmdbjava.Cursor;
import org.lmdbjava.Dbi;
import org.lmdbjava.DbiFlags;
import org.lmdbjava.Env;
import org.lmdbjava.EnvFlags;
import org.lmdbjava.SeekOp;
import org.lmdbjava.Txn;

import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.node.api.local_infrastructure.storage.KeyValueDb;

public class KeyValueDbImpl implements KeyValueDb {
	
	@Override
	public Optional<ByteBuffer> get(ByteBuffer key) {
		try (Txn<ByteBuffer> txn = env().txnRead()) {
			ByteBuffer value = db().get(txn, key);
			return value != null ? Optional.of(value) : Optional.empty();
		}
	}
	
	@Override
	public void put(ByteBuffer key, ByteBuffer value) {
		//key.flip();
		//value.flip();
		db().put(key, value);
	}
	
	@Override
	public void put(Map<ByteBuffer, ByteBuffer> entries) {
		try (Txn<ByteBuffer> txn = env().txnWrite()) {
			for (Map.Entry<ByteBuffer, ByteBuffer> entry : entries.entrySet()) {
				entry.getKey().flip();
				entry.getValue().flip();
				db().put(txn, entry.getKey(), entry.getValue());
			}
			txn.commit();
		}
	}
	
	@Override
	public void delete(ByteBuffer key) {
		db().delete(key);
	}
	
	@Override
	public void iterate(byte[] keyPrefix, BiConsumer<ByteBuffer, ByteBuffer> consumer) {
		try (Txn<ByteBuffer> txn = env().txnRead()) {
			try (Cursor<ByteBuffer> cursor = db().openCursor(txn)) {
				boolean continu = true;
				continu = cursor.seek(SeekOp.MDB_FIRST);
				while (continu) {
					ByteBuffer key = cursor.key();
					boolean keyMatches = true;
					for (int i = 0; i < keyPrefix.length; i++) {
						if (key.get() != keyPrefix[i]) {
							keyMatches = false;
							break;
						}
					}
					
					if (keyMatches) {
						while (key.remaining() != 0) {
							key.get();
						}
						key.flip();
						consumer.accept(key, cursor.val());
					}
					continu = cursor.next();
				}
			}
		}
	}
	
	@Override
	public void release(Object user) {
		unuse(user);
	}
	
	public void dispose() {
		try { db().close(); } catch (Throwable ex) { }
		try { env().close(); } catch (Throwable ex) { }
	}
	
	@Override
	public ByteBuffer byteBuffer(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
		return buffer.put(bytes).flip();
	}
	
	@Override
	public byte[] bytesArray(ByteBuffer buffer) {
		byte[] valueBytes = new byte[buffer.remaining()];
		buffer.get(valueBytes);
		return valueBytes;
	}
	
	
	public void use(Object user) {
		users.add(user);
	}
	public void unuse(Object user) {
		users.remove(user);
		
		if (users.isEmpty()) {
			releaseListener().accept(this);
		}
	}
	private final Set<Object> users = new HashSet<>();
	
	
	public KeyValueDbImpl(String id, File dataDir, Config config, Consumer<KeyValueDbImpl> releaseListener) {
		this.id = id;
		this.releaseListener = releaseListener;
		File dbDir = new File(dataDir, id);
		dbDir.mkdirs();
		
		ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		try {
			this.env = Env.create().setMapSize(10_485_760).setMaxDbs(1).setMaxReaders(1).open(dbDir, EnvFlags.MDB_NOTLS);
			this.db = env.openDbi("DEFAULT", DbiFlags.MDB_CREATE);
		} finally {
			Thread.currentThread().setContextClassLoader(currentClassLoader);
		}
	}
	
	private final String id;
	public String id() { return id; }
	
	private final Env<ByteBuffer> env;
	protected Env<ByteBuffer> env() { return env; }
	
	private final Dbi<ByteBuffer> db;
	protected Dbi<ByteBuffer> db() { return db; }
	
	private final Consumer<KeyValueDbImpl> releaseListener;
	protected Consumer<KeyValueDbImpl> releaseListener() { return releaseListener; }
	
}
