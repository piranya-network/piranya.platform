package network.piranya.platform.node.core.local_infrastructure.storage;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.queue.TailerDirection;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueue;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import network.piranya.platform.api.exceptions.QueueNotExistsException;
import network.piranya.platform.node.api.local_infrastructure.storage.PersistentQueue;
import network.piranya.platform.node.utilities.DisposableSupport;

public class ChronicleBasedPersistentQueue implements PersistentQueue {
	
	@Override
	public void append(byte[] data) {
		writeBytes(data, 0, data.length);
	}
	
	@Override
	public void read(byte[] readingBuffer, Consumer<Integer> consumer) {
		readBytes(readingBuffer, len -> {
			consumer.accept(len);
			return true;
		});
	}
	
	@Override
	public QueueIterator read(byte[] readingBuffer) {
		return new TailerIterator(queue().createTailer().toStart());
	}
	
	@Override
	public void readWhile(byte[] readingBuffer, Function<Integer, Boolean> consumer) {
		readBytes(readingBuffer, consumer);
	}
	
	//@Override
	public void readBackward(byte[] readingBuffer, Consumer<Integer> consumer) {
		readBytesBackward(readingBuffer, len -> {
			consumer.accept(len);
			return true;
		});
	}
	
	//@Override
	public void readBackwardWhile(byte[] readingBuffer, Function<Integer, Boolean> consumer) {
		readBytesBackward(readingBuffer, consumer);
	}
	
	@Override
	public void release(Object user) {
		unuse(user);
	}
	
	public void dispose() {
		try { queue().close(); } catch (Throwable ex) { }
	}
	
	
	protected void writeBytes(byte[] buffer, int offset, int length) {
		ExcerptAppender appender = queue().acquireAppender();
		appender.writeBytes(bytes -> bytes.write(buffer, offset, length));
	}
	
	protected void readBytes(byte[] buffer, Function<Integer, Boolean> consumer) {
		ExcerptTailer tailer = queue().createTailer().toStart();
		final boolean[] contin = new boolean[] { true};
		while (contin[0] && tailer.readBytes(bytes -> contin[0] = consumer.apply(bytes.read(buffer))));
	}
	
	protected void readBytesBackward(byte[] buffer, Function<Integer, Boolean> consumer) {
		ExcerptTailer tailer = queue().createTailer().direction(TailerDirection.BACKWARD).toEnd();
		final boolean[] contin = new boolean[] { true};
		while (contin[0] && tailer.readBytes(bytes -> contin[0] = consumer.apply(bytes.read(buffer))));
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
	
	
	public ChronicleBasedPersistentQueue(File dataDir, String id, Config config, Consumer<ChronicleBasedPersistentQueue> releaseListener) {
		this.id = id;
		this.config = config;
		this.releaseListener = releaseListener;
		
		File queueDir = new File(dataDir, id);
		if (config.requireExistence() && !queueDir.exists()) {
			throw new QueueNotExistsException(id);
		}
		
		SingleChronicleQueueBuilder builder = SingleChronicleQueueBuilder.single(queueDir).blockSize(256 * 1024);
		/*
		builder.rollCycle(RollCycles.DAILY);
		builder.storeFileListener(new StoreFileListener() {
			@Override public void onReleased(int cycle, File file) {
				//checkForArchival(file);
			}
		});
		*/
		this.queue = builder.build();
	}
	
	private final String id;
	public String id() { return id; }
	
	private final Config config;
	protected Config config() { return config; }
	
	private final Consumer<ChronicleBasedPersistentQueue> releaseListener;
	protected Consumer<ChronicleBasedPersistentQueue> releaseListener() { return releaseListener; }
	
	private final SingleChronicleQueue queue;
	protected SingleChronicleQueue queue() {
		return queue;
	}
	
	
	protected class TailerIterator implements QueueIterator {
		
		@Override
		public boolean hasNext() {
			disposable.checkIfDisposed();
			
			if (isConsumed) {
				isConsumed = false;
				hasNext = tailer.readBytes(bytes -> nextLen = bytes.read(readingBuffer));
			}
			return hasNext;
		}
		
		@Override
		public Integer next() {
			disposable.checkIfDisposed();
			
			if (!isConsumed) {
				isConsumed = true;
				return nextLen;
			} else {
				isConsumed = true;
				tailer.readBytes(bytes -> nextLen = bytes.read(readingBuffer));
				return nextLen;
			}
		}
		
		@Override
		public byte[] readingBuffer() { return readingBuffer; }
		
		@Override
		public void dispose() {
			disposable.markDisposed();
		}
		
		@Override
		public void close() throws Exception {
			dispose();
		}
		
		
		public TailerIterator(ExcerptTailer tailer) {
			this.tailer = tailer;
		}
		
		private final ExcerptTailer tailer;
		private final DisposableSupport disposable = new DisposableSupport(this);
		private final byte[] readingBuffer = new byte[config().maxMessageSize()];
		private boolean hasNext;
		private int nextLen;
		boolean isConsumed = true;
	}
	
}
