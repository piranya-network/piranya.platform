package network.piranya.platform.api.utilities;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import network.piranya.platform.api.models.trading.filling.Fill;

public class DetailedPnlCalculator {
	
	public DetailedPnlCalculator acceptFill(Fill fill) {
		return acceptFill(fill.symbol(), fill.price(), fill.size());
	}
	
	public DetailedPnlCalculator acceptFill(String symbol, BigDecimal price, BigDecimal size) {
		PnlCalculator pnlCalculator = calculators.get(symbol);
		if (pnlCalculator == null) {
			pnlCalculator = new PnlCalculator();
			calculators.put(symbol, pnlCalculator);
		}
		
		pnlCalculator.acceptFill(price, size);
		
		return this;
	}
	
	
	private final Map<String, PnlCalculator> calculators = new HashMap<>();
	
}
