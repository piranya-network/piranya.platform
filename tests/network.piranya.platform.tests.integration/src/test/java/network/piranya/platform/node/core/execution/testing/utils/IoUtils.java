package network.piranya.platform.node.core.execution.testing.utils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class IoUtils {
	
	public static int copy(InputStream in, OutputStream out) throws IOException {
		return copy(in, out, true);
	}
	
	public static int copy(InputStream in, OutputStream out, boolean closeStreams) throws IOException {
		try {
			int byteCount = 0;
			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead = -1;
			while ((bytesRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, bytesRead);
				byteCount += bytesRead;
			}
			out.flush();
			return byteCount;
		}
		finally {
			if (closeStreams) {
				closeIfPossible(in);
				closeIfPossible(out);
			}
		}
	}
	
	public static void closeIfPossible(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (Throwable ex) {
			}
		}
	}
	
	public static String readStream(InputStream input) {
		StringBuilder contents = new StringBuilder();
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			try {
				String lineSeparator = System.getProperty("line.separator");
				String line = null;	
				while ((line = reader.readLine()) != null) {
					contents.append(line);
					contents.append(lineSeparator);
				}
			} finally {
				reader.close();
			}
		} catch (IOException ex){
			throw new RuntimeException("IO Exception: " + ex.getMessage(), ex);
		}
		
		return contents.toString();
	}
	
	public static String readStream(InputStream input, String charset) {
		StringBuilder contents = new StringBuilder();
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(input, charset));
			try {
				String lineSeparator = System.getProperty("line.separator");
				String line = null;	
				while ((line = reader.readLine()) != null) {
					contents.append(line);
					contents.append(lineSeparator);
				}
			} finally {
				reader.close();
			}
		} catch (IOException ex){
			throw new RuntimeException("IO Exception: " + ex.getMessage(), ex);
		}
		
		return contents.toString();
	}
	
	
	protected static final int BUFFER_SIZE = 4096;
	
}
