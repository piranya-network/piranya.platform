package network.piranya.platform.api.models.infrastructure.storage;

public class QueueStoreConfig {
	
	private final boolean keepOpen;
	public boolean isKeepOpen() { return keepOpen; }
	
	private final boolean isGlobal;
	public boolean isGlobal() { return isGlobal; }
	
	private final boolean requireExistence;
	public boolean requireExistence() { return requireExistence; }
	
	private final int maxMessageSize;
	public int maxMessageSize() { return maxMessageSize; }
	
	public QueueStoreConfig(boolean keepOpen) {
		this(keepOpen, false);
	}
	
	public QueueStoreConfig(boolean keepOpen, boolean isGlobal) {
		this(keepOpen, isGlobal, false);
	}
	
	public QueueStoreConfig(boolean keepOpen, boolean isGlobal, boolean requireExistence) {
		this(keepOpen, isGlobal, requireExistence, 100 * 1024);
	}
	
	public QueueStoreConfig(boolean keepOpen, boolean isGlobal, boolean requireExistence, int maxMessageSize) {
		this.keepOpen = keepOpen;
		this.isGlobal = isGlobal;
		this.requireExistence = requireExistence;
		this.maxMessageSize = maxMessageSize;
	}
	
}
