package network.piranya.platform.api.models.trading.filling;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import network.piranya.platform.api.models.trading.Instrument;

/**
 * A collection of logically grouped fills.
 */
public class OpenTrade {
	
	private final String tradeId;
	public String tradeId() {
		return tradeId;
	}
	
	private final Instrument instrument;
	public Instrument instrument() { return instrument; }
	
	private final List<Fill> fills;
	public List<Fill> fills() {
		return fills;
	}
	
	private final Set<String> tags;
	public Set<String> tags() {
		return tags;
	}
	
	private final BigDecimal totalSize;
	public BigDecimal totalSize() {
		return totalSize;
	}
	
	public OpenTrade(String tradeId, Instrument instrument, List<Fill> fills, Set<String> tags) {
		this.tradeId = tradeId;
		this.instrument = instrument;
		this.fills = fills;
		this.tags = tags;
		
		BigDecimal sum = new BigDecimal("0.0");
		for (Fill f : fills) {
			sum = sum.add(f.size());
		}
		this.totalSize = sum;
	}
	
}
