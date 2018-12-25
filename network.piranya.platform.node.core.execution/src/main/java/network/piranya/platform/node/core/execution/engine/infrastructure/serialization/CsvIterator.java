package network.piranya.platform.node.core.execution.engine.infrastructure.serialization;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import network.piranya.platform.api.exceptions.UnexpectedException;
import network.piranya.platform.api.lang.DisposableIterator;
import network.piranya.platform.api.models.infrastructure.CsvReaderParams;

public class CsvIterator implements DisposableIterator<String[]> {
	
	private CsvReaderParams params;
	@Override
	public boolean hasNext() {
		return next != null;
	}
	
	@Override
	public String[] next() {
		String[] currentNext = this.next;
		readNext();
		return currentNext;
	}
	
	@Override
	public void dispose() {
		System.out.println("-- dispose");
		if (!disposed) {
			disposed = true;
			
			try { reader.close(); } catch (Throwable ex) { }
			try { input.close(); } catch (Throwable ex) { }
		}
	}
	
	@Override
	public void close() throws Exception {
		dispose();
	}
	
	@Override
	protected void finalize() throws Throwable {
		dispose();
	}
	
	protected void readNext() {
		try {
			String line = reader.readLine();
			if (line != null) {
				if (isFirst) {
					isFirst = false;
					if (params.isIgnoreFirstRow()) {
						readNext();
						return;
					}
				}
				
				String[] fields = line.split(fieldSplitter);
				for (int i = 0; i < fields.length; i++) {
					String f = fields[i];
					if (f.charAt(0) == params.quoteChar() && f.charAt(f.length() - 1) == params.quoteChar()) {
						fields[i] = f.substring(1, f.length() - 1);
					}
				}
				
				this.next = fields;
			} else {
				this.next = null;
			}
		} catch (Exception ex) {
			throw new UnexpectedException(ex);
		}
	}
	
	
	public CsvIterator(InputStream input, CsvReaderParams params) {
		this.input = input;
		this.params = params;
		this.fieldSplitter = String.format("%s(?=([^%s]*%s[^%s]*%s)*[^%s]*$)",
				params.delimiter(), params.quoteChar(), params.quoteChar(), params.quoteChar(), params.quoteChar(), params.quoteChar());
		this.reader = new BufferedReader(new InputStreamReader(input));
		
		readNext();
	}
	
	private String[] next = null;
	private boolean disposed = false;
	private String fieldSplitter;
	private boolean isFirst = true;
	private InputStream input;
	private BufferedReader reader;
	
}
