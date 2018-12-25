package network.piranya.platform.api.models.infrastructure.storage;

import java.util.function.Consumer;

import network.piranya.platform.api.lang.DisposableIterator;

public interface QueueStoreReader {
	
	void read(Consumer<Object> processor);
	
	void read(long startTime, long endTime, Consumer<Object> processor);
	
	<EntryType> void read(Class<EntryType> entryType, Consumer<EntryType> processor);
	
	<EntryType> void read(long startTime, long endTime, Class<EntryType> entryType, Consumer<EntryType> processor);
	
	DisposableIterator<Object> read();
	
}
