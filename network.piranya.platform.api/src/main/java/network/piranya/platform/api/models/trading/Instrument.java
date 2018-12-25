package network.piranya.platform.api.models.trading;

import network.piranya.platform.api.Constants;
import network.piranya.platform.api.exceptions.InvalidOperationException;

public class Instrument implements Comparable<Instrument> {
	
	private final Asset base;
	public Asset base() { return base; }
	
	private final Asset quote;
	public Asset quote() { return quote; }
	
	private final String symbol;
	public String symbol() { return symbol; }
	
	private final String sourceId;
	public String sourceId() { return sourceId; }
	
	private final String[] sources;
	public String[] sources() { return sources; }
	
	public Instrument(String symbol) {
		this.symbol = symbol;
		
		if (symbol.indexOf('/') < 0) {
			throw new IllegalArgumentException(String.format("Symbol '%s' is an asset not an instrument", symbol));
		}
		
		String traded = symbol;
		int sourceIndex = symbol.indexOf('>');
		if (sourceIndex >= 0) {
			traded = symbol.substring(0, sourceIndex);
			this.sourceId = symbol.substring(symbol.lastIndexOf(Constants.LP.LP_SEPARATOR) + 1, symbol.length());
			this.sources = this.sourceId.split("\\" + Constants.LP.LP_SEPARATOR);
		} else {
			this.sourceId = null;
			this.sources = new String[0];
		}
		
		int assestsSeparatorIndex = traded.indexOf('/');
		this.base = new Asset(traded.substring(0, assestsSeparatorIndex));
		this.quote = new Asset(traded.substring(assestsSeparatorIndex + 1, traded.length()));
	}
	
	public Instrument(String symbol, String sourceId) {
		this(String.format("%s%s%s", symbol, Constants.LP.LP_SEPARATOR, sourceId));
	}
	
	public Instrument(Asset base, Asset quote, String... sourcesIds) {
		this.symbol = generateSymbol(base, quote, sourcesIds).toString();
		this.base = base;
		this.quote = quote;
		this.sourceId = sourcesIds.length > 0 ? sourcesIds[sourcesIds.length - 1] : null;
		this.sources = sourcesIds;
	}
	
	
	@Override
	public boolean equals(Object other) {
		return other instanceof Instrument ? symbol().equals(((Instrument)other).symbol()) : false;
	}
	
	@Override
	public int hashCode() {
		return symbol().hashCode();
	}
	
	@Override
	public String toString() {
		return symbol();
	}
	
	@Override
	public int compareTo(Instrument other) {
		return symbol().compareTo(other.symbol());
	}
	
	
	public boolean matches(String symbol) {
		return symbol().equals(symbol) || symbol().startsWith(symbol + Constants.LP.LP_SEPARATOR);
	}
	
	
	public Instrument zoomOutSource() {
		if (sources().length == 0) {
			throw new InvalidOperationException(String.format("Can not zoom out source for symbol '%s' sicne there are no sources", symbol()));
		}
		
		return new Instrument(symbol().substring(0, symbol().lastIndexOf(Constants.LP.LP_SEPARATOR)));
		//new Instrument(base(), quote(), Arrays.copyOfRange(sources(), 0, sources().length - 1));
	}
	
	public Instrument withoutSources() {
		return new Instrument(base(), quote());
	}
	
	
	private static StringBuffer generateSymbol(Asset base, Asset quote, String... sourcesIds) {
		StringBuffer sb = new StringBuffer("");
		sb.append(base.symbol());
		sb.append('/');
		sb.append(quote.symbol());
		for (String sourceId : sourcesIds) {
			sb.append('>');
			sb.append(sourceId);
		}
		return sb;
	}

	public Instrument appendSource(String sourceId) {
		return new Instrument(symbol(), sourceId);
	}
	
	
	// example symbols: 
	//   BTC/USD>BITFINEX
	//   BTC-FZ18/USD>BITFINEX>TESTLP
	//   BTC-OC181202@9500.00/USD>BITFINEX>TESTLP
	
}
