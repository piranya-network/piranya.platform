package network.piranya.platform.node.api.local_infrastructure.storage;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.function.BiConsumer;

import network.piranya.platform.api.lang.Optional;

public interface KeyValueDb {

	Optional<ByteBuffer> get(ByteBuffer key);
	void put(ByteBuffer key, ByteBuffer value);
	void put(Map<ByteBuffer, ByteBuffer> entries);
	void delete(ByteBuffer key);
	void iterate(byte[] keyPrefix, BiConsumer<ByteBuffer, ByteBuffer> consumer);
	
	void release(Object user);
	
	ByteBuffer byteBuffer(byte[] bytes);
	byte[] bytesArray(ByteBuffer buffer);
	
	
	public class Config {
		
		public Config() {
		}
		
	}
	
}
