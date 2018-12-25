package network.piranya.platform.api.models.infrastructure.storage;

public class KeyValueStoreConfig {
	
	private final boolean keepOpen;
	public boolean isKeepOpen() { return keepOpen; }
	
	private final int maxMessageSize;
	public int maxMessageSize() { return maxMessageSize; }
	
	private final boolean isGlobal;
	public boolean isGlobal() { return isGlobal; }
	
	public KeyValueStoreConfig(boolean keepOpen) {
		this(keepOpen, false);
	}
	
	public KeyValueStoreConfig(boolean keepOpen, boolean isGlobal) {
		this(keepOpen, isGlobal, 100 * 1024);
	}
	
	public KeyValueStoreConfig(boolean keepOpen, boolean isGlobal, int maxMessageSize) {
		this.keepOpen = keepOpen;
		this.isGlobal = isGlobal;
		this.maxMessageSize = maxMessageSize;
	}
	
}
