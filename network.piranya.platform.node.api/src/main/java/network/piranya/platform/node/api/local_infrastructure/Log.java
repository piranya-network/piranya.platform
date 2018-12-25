package network.piranya.platform.node.api.local_infrastructure;

public interface Log {
	
	void info(String message);
	void info(String message, Object... args);
	
	void warning(String message);
	void warning(String message, Throwable error);
	
}
