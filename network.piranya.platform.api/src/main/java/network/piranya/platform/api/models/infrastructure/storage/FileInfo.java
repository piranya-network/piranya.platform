package network.piranya.platform.api.models.infrastructure.storage;

public interface FileInfo {
	
	String fileName(boolean withExtension);
	String path();
	String[] dirs();
	
}
