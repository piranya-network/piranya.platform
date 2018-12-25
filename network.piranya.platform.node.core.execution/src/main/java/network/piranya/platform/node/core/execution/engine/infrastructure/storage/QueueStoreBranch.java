package network.piranya.platform.node.core.execution.engine.infrastructure.storage;

import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import network.piranya.platform.api.lang.DisposableIterator;
import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.node.api.local_infrastructure.storage.PersistentQueue;
import network.piranya.platform.node.api.local_infrastructure.storage.PersistentQueue.QueueIterator;
import network.piranya.platform.node.utilities.BitUtils;
import network.piranya.platform.node.utilities.DisposableSupport;
import network.piranya.platform.node.utilities.Encoder;
import network.piranya.platform.node.utilities.TimeService;

public class QueueStoreBranch {
	
	public void append(Object entry) {
		if (!isReplaying()) {
			persistentueue().append(encoder().encode(entry, this::prependTime));
		} else {
			appendReplayEntry(entry);
		}
	}
	
	public void read(Consumer<Object> processor, long startTime, long endTime, Optional<Short> filteredEntryType) {
		byte[] buffer = new byte[maxMessageSize];
		short fEntryType = filteredEntryType.orElse(Short.valueOf((short)-1));
		persistentueue().read(buffer, len -> {
			long time = BitUtils.longFromByteArray(buffer, 0);
			if (time >= startTime && time <= endTime) {
				short entryType = BitUtils.shortFromByteArray(buffer, 8);
				if (fEntryType == -1 || fEntryType == entryType) {
					Object entry = encoder().decode(buffer, 8, len - 8);
					processor.accept(entry);
				}
			}
		});
	}
	
	public DisposableIterator<Object> read() {
		byte[] readingBuffer = new byte[maxMessageSize];
		return new ReadingIterator(persistentueue().read(readingBuffer));
	}
	
	
	public void use(Object branchUser) {
		users.add(branchUser);
	}
	public void unuse(Object branchUser) {
		users.remove(branchUser);
		
		if (users.isEmpty()) {
			branchReleaser().accept(this);
		}
	}
	private final Set<Object> users = new HashSet<>();
	
	
	public void finishReplay() {
		setReplaying(false);
		
		catchup();
	}
	
	protected void catchup() {
		// read queue backwards, compare last item in replay-appended list until you find its match, stop reading then and append a new items after the match to the queue.
		// compare items by encoding and comparing bytes to avoid equals not being implemented
	}
	
	protected void appendReplayEntry(Object entry) {
		replayAppendedEntries.add(entry);
		if (replayAppendedEntries.size() > 100) {
			replayAppendedEntries.remove(0);
		}
	}
	
	private final List<Object> replayAppendedEntries = new ArrayList<>();
	
	
	protected void prependTime(DataOutput output) {
		try { output.writeLong(TimeService.now()); }
		catch (IOException ex) { throw new RuntimeException(ex); }
	}
	
	
	public QueueStoreBranch(String uniqueQueueId, PersistentQueue persistentQueue, int maxMessageSize, Encoder encoder, Consumer<QueueStoreBranch> branchReleaser) {
		this.uniqueQueueId = uniqueQueueId;
		this.persistentueue = persistentQueue;
		this.maxMessageSize = maxMessageSize;
		this.encoder = encoder;
		this.branchReleaser = branchReleaser;
	}
	
	private final String uniqueQueueId;
	public String uniqueQueueId() { return uniqueQueueId; }
	
	private final PersistentQueue persistentueue;
	protected PersistentQueue persistentueue() { return persistentueue; }
	
	private final int maxMessageSize;
	
	private final Encoder encoder;
	public Encoder encoder() { return encoder; }
	
	private final Consumer<QueueStoreBranch> branchReleaser;
	protected Consumer<QueueStoreBranch> branchReleaser() { return branchReleaser; }
	
	private boolean isReplaying = false;
	protected boolean isReplaying() { return isReplaying; }
	protected void setReplaying(boolean isReplaying) { this.isReplaying = isReplaying; }
	
	public void startReplay() {
		setReplaying(true);
	}
	
	
	protected class ReadingIterator implements DisposableIterator<Object> {
		
		@Override
		public boolean hasNext() {
			disposable.checkIfDisposed();
			return iterator.hasNext();
		}

		@Override
		public Object next() {
			disposable.checkIfDisposed();
			
			int len = iterator.next();
			return encoder().decode(iterator.readingBuffer(), 8, len - 8);
		}

		@Override
		public void dispose() {
			disposable.markDisposed();
			
			iterator.dispose();
		}
		
		@Override
		protected void finalize() throws Throwable {
			dispose();
		}
		
		@Override
		public void close() throws Exception {
			dispose();
		}
		
		
		public ReadingIterator(PersistentQueue.QueueIterator iterator) {
			this.iterator = iterator;
		}
		
		private final QueueIterator iterator;
		protected QueueIterator iterator() { return iterator; }
		
		private DisposableSupport disposable = new DisposableSupport(this);
	}
	
}
