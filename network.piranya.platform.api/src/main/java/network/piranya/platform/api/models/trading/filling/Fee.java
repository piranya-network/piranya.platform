package network.piranya.platform.api.models.trading.filling;

import java.math.BigDecimal;
import network.piranya.platform.api.lang.Optional;

public class Fee {
	
	private final String typeId;
	public String typeId() { return typeId; }
	
	private final BigDecimal amount;
	public BigDecimal amount() { return amount; }
	
	private final Optional<String> details;
	public Optional<String> details() { return details; }
	
	public Fee(String typeId, BigDecimal amount, Optional<String> details) {
		this.typeId = typeId;
		this.amount = amount;
		this.details = details;
	}
	
}
