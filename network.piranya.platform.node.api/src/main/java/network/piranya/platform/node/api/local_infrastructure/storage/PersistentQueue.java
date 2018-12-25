package network.piranya.platform.node.api.local_infrastructure.storage;

import java.util.function.Consumer;
import java.util.function.Function;

import network.piranya.platform.api.lang.DisposableIterator;

public interface PersistentQueue {
	
	void append(byte[] data);
	
	void read(byte[] readingBuffer, Consumer<Integer> consumer);
	void readWhile(byte[] readingBuffer, Function<Integer, Boolean> consumer);
	void readBackward(byte[] readingBuffer, Consumer<Integer> consumer);
	void readBackwardWhile(byte[] readingBuffer, Function<Integer, Boolean> consumer);
	QueueIterator read(byte[] readingBuffer);
	
	void release(Object user);
	
	
	public class Config {
		
		private final boolean requireExistence;
		public boolean requireExistence() { return requireExistence; }
		
		private final int maxMessageSize;
		public int maxMessageSize() { return maxMessageSize; }
		
		private final long expirationPeriod;
		public long getExpirationPeriod() { return expirationPeriod; }
		
		public Config(boolean requireExistence, int maxMessageSize, long expirationPeriod) {
			this.requireExistence = requireExistence;
			this.maxMessageSize = maxMessageSize;
			this.expirationPeriod = expirationPeriod;
		}
		
		public Config() {
			this(false);
		}
		
		public Config(boolean requireExistence) {
			this(requireExistence, 100 * 1024, 0);
		}
	}
	
	public interface QueueIterator extends DisposableIterator<Integer> {
		
		byte[] readingBuffer();
		
	}
	
}
