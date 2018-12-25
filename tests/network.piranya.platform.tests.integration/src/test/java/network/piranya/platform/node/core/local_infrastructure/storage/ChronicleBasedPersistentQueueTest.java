package network.piranya.platform.node.core.local_infrastructure.storage;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import network.piranya.platform.node.api.local_infrastructure.storage.PersistentQueue;
import network.piranya.platform.node.core.execution.testing.support.AbstractExecutionTest;

public class ChronicleBasedPersistentQueueTest extends AbstractExecutionTest {
	
	@Test
	public void testWriteRead() {
		File dataDir = createTempDir();
		
		String[] entries = new String[] { "entry1", "entry2", "entry3" };
		
		ChronicleBasedPersistentQueue queue = new ChronicleBasedPersistentQueue(dataDir, "q0", new PersistentQueue.Config(), q -> {});
		for (String entry : entries) {
			queue.append(entry.getBytes());
		}
		
		byte[] readingBuffer = new byte[1024];
		List<String> entriesRead = new ArrayList<>();
		queue.read(readingBuffer, len -> entriesRead.add(new String(readingBuffer, 0, len)));
		assertArrayEquals(entries, entriesRead.toArray(new String[0]));
		queue.dispose();
		
		ChronicleBasedPersistentQueue queue2 = new ChronicleBasedPersistentQueue(dataDir, "q0", new PersistentQueue.Config(), q -> {});
		List<String> entriesRead2 = new ArrayList<>();
		queue2.read(readingBuffer, len -> entriesRead2.add(new String(readingBuffer, 0, len)));
		assertArrayEquals(entries, entriesRead2.toArray(new String[0]));
		queue2.dispose();
	}
	
	@Test
	public void testReadInReverse() {
		File dataDir = createTempDir();
		
		String[] entries = new String[] { "entry1", "entry2", "entry3" };
		
		ChronicleBasedPersistentQueue queue = new ChronicleBasedPersistentQueue(dataDir, "q0", new PersistentQueue.Config(), q -> {});
		for (String entry : entries) {
			queue.append(entry.getBytes());
		}
		
		byte[] readingBuffer = new byte[1024];
		List<String> entriesRead = new ArrayList<>();
		queue.readBackward(readingBuffer, len -> entriesRead.add(new String(readingBuffer, 0, len)));
		Collections.reverse(entriesRead);
		assertArrayEquals(entries, entriesRead.toArray(new String[0]));
		queue.dispose();
	}
	
	@Test
	public void testReadInReverseWhile() {
		File dataDir = createTempDir();
		
		String[] entries = new String[] { "entry1", "entry2", "entry3" };
		
		ChronicleBasedPersistentQueue queue = new ChronicleBasedPersistentQueue(dataDir, "q0", new PersistentQueue.Config(), q -> {});
		for (String entry : entries) {
			queue.append(entry.getBytes());
		}
		
		byte[] readingBuffer = new byte[1024];
		List<String> entriesRead = new ArrayList<>();
		queue.readBackwardWhile(readingBuffer, len -> {
			entriesRead.add(new String(readingBuffer, 0, len));
			return entriesRead.size() == 2 ? false : true;
		});
		assertArrayEquals(new String[] { "entry3", "entry2" }, entriesRead.toArray(new String[0]));
		queue.dispose();
	}
	
}
