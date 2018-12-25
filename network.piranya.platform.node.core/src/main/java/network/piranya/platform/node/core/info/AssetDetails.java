package network.piranya.platform.node.core.info;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.api.models.trading.Instrument;

public class AssetDetails {
	
	private final String symbol;
	public String symbol() { return symbol; }
	
	private final Set<String> altSymbols;
	public Set<String> altSymbols() { return altSymbols; }
	
	private final String displayName;
	public String displayName() { return displayName; }
	
	private final String displayNameUpperCase;
	public String displayNameUpperCase() { return displayNameUpperCase; }
	
	private Optional<BigDecimal> priceInBtc = Optional.empty();
	public Optional<BigDecimal> priceInBtc() { return priceInBtc; }
	public void setPriceInBtc(BigDecimal priceInBtc) { this.priceInBtc = Optional.of(priceInBtc); }
	
	private Optional<BigDecimal> priceInUsd = Optional.empty();
	public Optional<BigDecimal> priceInUsd() { return priceInUsd; }
	public void setPriceInUsd(BigDecimal priceInUsd) { this.priceInUsd = Optional.of(priceInUsd); }
	
	private final List<Instrument> tradedInstruments;
	public List<Instrument> tradedInstruments() { return tradedInstruments; }
	
	public AssetDetails(String symbol, Set<String> altSymbols, String displayName, List<Instrument> tradedInstruments) {
		this.symbol = symbol;
		this.altSymbols = altSymbols;
		this.displayName = displayName;
		this.displayNameUpperCase = displayName.toUpperCase();
		this.tradedInstruments = tradedInstruments;
	}
	
}