package network.piranya.platform.api.models.infrastructure.storage;

import java.io.InputStream;
import java.util.Collection;
import java.util.function.Consumer;

public interface FileStore {
	
	Collection<FileInfo> listFilesInSystemDir(String path, boolean isRecursive);
	void processSystemFile(String path, Consumer<InputStream> consumer);
	void processSystemArchive(String path, Consumer<InputStream> consumer);
	InputStream processSystemFile(String path);
	InputStream processSystemArchive(String path);
	
}
