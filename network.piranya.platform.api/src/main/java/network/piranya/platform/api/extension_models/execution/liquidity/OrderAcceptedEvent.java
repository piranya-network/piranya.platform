package network.piranya.platform.api.extension_models.execution.liquidity;

import java.math.BigDecimal;
import network.piranya.platform.api.lang.Optional;

import network.piranya.platform.api.models.trading.ordering.OrderSpec;

public class OrderAcceptedEvent {
	
	private final OrderSpec spec;
	public OrderSpec getSpec() {
		return spec;
	}
	
	private final String reference;
	public String reference() {
		return reference;
	}
	
	private final Optional<String> exitingTradeId;
	public Optional<String> exitingTradeId() {
		return exitingTradeId;
	}
	
	private final Optional<BigDecimal> tp;
	public Optional<BigDecimal> tp() {
		return tp;
	}
	
	private final Optional<BigDecimal> sl;
	public Optional<BigDecimal> sl() {
		return sl;
	}
	
	public OrderAcceptedEvent(OrderSpec spec, String reference, Optional<String> exitingTradeId, Optional<BigDecimal> tp, Optional<BigDecimal> sl) {
		this.spec = spec;
		this.reference = reference;
		this.exitingTradeId = exitingTradeId;
		this.tp = tp;
		this.sl = sl;
	}
}