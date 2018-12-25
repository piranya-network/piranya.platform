package network.piranya.platform.node.utilities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class FileUtils {
	
	public static int copy(File in, File out) throws IOException {
		out.getParentFile().mkdirs();
		
		return IoUtils.copy(new BufferedInputStream(new FileInputStream(in)),
		    new BufferedOutputStream(new FileOutputStream(out, false)));
	}
	
	public static int copy(InputStream in, File out) throws IOException {
		out.getParentFile().mkdirs();
		
		return IoUtils.copy(in, new BufferedOutputStream(new FileOutputStream(out, false)));
	}
	
	public static File[] listAllChildFiles(File dir) {
		List<File> result = new ArrayList<File>();
		
		File[] files = dir.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					result.addAll(Arrays.asList(listAllChildFiles(file)));
				} else {
					result.add(file);
				}
			}
		}
		
		return result.toArray(new File[0]);
	}
	
	public static File createTempDir() {
		return createTempDir(false);
	}
	
	public static File createTempDir(boolean deleteOnExit) {
		File dir = createTempDir(null, null);
		if (deleteOnExit) {
			dir.deleteOnExit();
		}
		return dir;
	}

	public static File createTempDir(String prefix, String suffix) {
		if (prefix == null || suffix == null) {
			String uuidString = UUID.randomUUID().toString();
			if (prefix == null) {
				prefix = uuidString.substring(0, 4);
			}
			if (suffix == null) {
				suffix = uuidString.substring(uuidString.lastIndexOf('-'));
			}
		}
		
		try {
			File tempFile;
			tempFile = File.createTempFile(prefix, suffix);
			tempFile.delete();
			File tempDir = new File(tempFile.getAbsolutePath());
			tempDir.mkdir();
			return tempDir;
		} catch (IOException ex) {
			throw new RuntimeException("Failed to create temporary directory.", ex);
		}
	}
	
	public static File createTempFile(boolean deleteOnExit) {
		String uuidString = UUID.randomUUID().toString();
		String prefix = uuidString.substring(0, 4);
		String suffix = uuidString.substring(uuidString.lastIndexOf('-'));
		
		try {
			File file = File.createTempFile(prefix, suffix);
			if (deleteOnExit) {
				file.deleteOnExit();
			}
			return file;
		} catch (IOException ex) {
			throw new RuntimeException("Failed to create temporary directory.", ex);
		}
	}
	
	public static boolean delete(File file) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					delete(files[i]);
				}
				else {
					files[i].delete();
				}
			}
		}
		return file.delete();
	}
	
	public static File deleteOnExit(File file) {
		if (file.exists() && file.isDirectory()) {
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteOnExit(files[i]);
				}
				else {
					files[i].deleteOnExit();
				}
			}
		}
		file.deleteOnExit();
		return file;
	}
	
	public static void deleteDir(File dir) {
		File[] files = dir.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory()) {
					deleteDir(f);
				} else {
					f.delete();
				}
			}
		}
		dir.delete();
	}
	
	public static void deleteDirContents(File dir) {
		File[] files = dir.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory()) {
					deleteDir(f);
				} else {
					f.delete();
				}
			}
		}
	}
	
	public static void copyDirectoryContents(File sourceDir, File destDir) throws IOException {
		if(!destDir.exists()) {
			destDir.mkdir();
		}
		
		File[] children = sourceDir.listFiles();
		
		for(File sourceChild : children) {
			String name = sourceChild.getName();
			File destChild = new File(destDir, name);
			if(sourceChild.isDirectory()) {
				copyDirectoryContents(sourceChild, destChild);
			} else {
				copy(sourceChild, destChild);
			}
		}
	}
	
	public static boolean isAccessible(File file) {
		if (!file.exists()) {
			throw new RuntimeException(String.format("File '%s' does not exist", file.getAbsolutePath()));
		}
		
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(file);
			fileInputStream.read();
			
			return true;
		} catch (IOException ex) {
			return false;
		} finally {
			try { fileInputStream.close(); }
			catch (Throwable ex) { }
		}
	}
	
	public static void deleteDirectory(File dir) {
		try {
			Files.walk(dir.toPath())
			.sorted(Comparator.reverseOrder())
			.map(Path::toFile)
			.forEach(File::delete);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
}
