package network.piranya.platform.node.core.execution.engine.infrastructure.storage;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

import network.piranya.platform.api.exceptions.UnexpectedException;
import network.piranya.platform.api.models.infrastructure.storage.FileInfo;
import network.piranya.platform.api.models.infrastructure.storage.FileStore;

public class FileStoreImpl implements FileStore {
	
	@Override
	public Collection<FileInfo> listFilesInSystemDir(String path, boolean isRecursive) {
		checkPermission(path);
		try {
			List<FileInfo> result = new ArrayList<>();
			Files.walkFileTree(Paths.get(path), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					result.add(new FileInfoImpl(file.toFile()));
					return super.visitFile(file, attrs);
				}
			});
			return result;
		} catch (IOException ex) {
			throw new UnexpectedException(ex);
		}
	}
	
	@Override
	public void processSystemFile(String path, Consumer<InputStream> consumer) {
		checkPermission(path);
		
		try (FileInputStream fileInput = new FileInputStream(path)) {
			try (BufferedInputStream input = new BufferedInputStream(fileInput)) {
				consumer.accept(input);
			}
		} catch (IOException ex) {
			throw new UnexpectedException(ex);
		}
	}
	
	@Override
	public InputStream processSystemFile(String path) {
		checkPermission(path);
		
		try { return new BufferedInputStream(new FileInputStream(path)); }
		catch (IOException ex) { throw new UnexpectedException(ex); }
	}
	
	@Override
	public void processSystemArchive(String path, Consumer<InputStream> consumer) {
		processSystemFile(path, input -> {
			try { consumer.accept(new GZIPInputStream(input)); }
			catch (IOException ex) { throw new UnexpectedException(ex); }
		});
	}
	
	@Override
	public InputStream processSystemArchive(String path) {
		try { return new GZIPInputStream(processSystemFile(path)); }
		catch (IOException ex) { throw new UnexpectedException(ex); }
	}
	
	
	protected void checkPermission(String path) {
		
	}
	
	
	public FileStoreImpl(StorageOperator storageOperator, String uniqueModuleId) {
	}
	
	
	protected class FileInfoImpl implements FileInfo {
		
		@Override
		public String fileName(boolean withExtension) {
			if (withExtension) {
				return file.getName();
			} else {
				int dotIndex = file.getName().indexOf('.');
				return dotIndex >= 0 ? file.getName().substring(0, dotIndex) : file.getName();
			}
		}
		private final File file;
		
		@Override
		public String path() {
			return path;
		}
		private final String path;
		
		@Override
		public String[] dirs() {
			File iterator = file.getParentFile();
			List<String> result = new ArrayList<>();
			while (iterator != null) {
				result.add(0, iterator.getName());
				iterator = iterator.getParentFile();
			}
			return result.toArray(new String[0]);
		}
		
		public FileInfoImpl(File file) throws IOException {
			this.file = file;
			this.path = file.getCanonicalPath();
		}
	}
	
}
