package network.piranya.platform.api.models.info;

import java.util.List;
import java.util.Set;

import network.piranya.platform.api.models.trading.Instrument;

public class AssetInfo implements Comparable<AssetInfo> {
	
	private final String symbol;
	public String symbol() { return symbol; }
	
	private final Set<String> altSymbols;
	public Set<String> altSymbols() { return altSymbols; }
	
	private final String displayName;
	public String displayName() { return displayName; }
	
	private final List<Instrument> tradedInstruments;
	public List<Instrument> tradedInstruments() { return tradedInstruments; }
	
	public AssetInfo(String symbol, Set<String> altSymbols, String displayName, List<Instrument> tradedInstruments) {
		this.symbol = symbol;
		this.altSymbols = altSymbols;
		this.displayName = displayName;
		this.tradedInstruments = tradedInstruments;
	}

	@Override
	public int compareTo(AssetInfo o) {
		return symbol().compareTo(o.symbol());
	}
	
}