package network.piranya.platform.api.models.infrastructure;

public class CsvReaderParams {
	
	private final char delimiter;
	public char delimiter() { return delimiter; }
	
	private final char quoteChar;
	public char quoteChar() { return quoteChar; }
	
	private final boolean ignoreFirstRow;
	public boolean isIgnoreFirstRow() { return ignoreFirstRow; }
	
	public CsvReaderParams(char delimiter, char quoteChar, boolean ignoreFirstRow) {
		this.delimiter = delimiter;
		this.quoteChar = quoteChar;
		this.ignoreFirstRow = ignoreFirstRow;
	}
	
}
