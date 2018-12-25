package network.piranya.platform.api.extension_models.execution.liquidity;

import java.math.BigDecimal;
import network.piranya.platform.api.lang.Optional;

import network.piranya.platform.api.models.trading.ordering.OrderSpec;

public class PlaceOrderRequest {
	
	private final OrderSpec spec;
	public OrderSpec spec() {
		return spec;
	}
	
	private final Optional<String> refId;
	public Optional<String> refId() {
		return refId;
	}
	
	private final Optional<String> externalTradeId;
	public Optional<String> externalTradeId() {
		return externalTradeId;
	}
	
	private final Optional<BigDecimal> tp;
	public Optional<BigDecimal> tp() {
		return tp;
	}
	
	private final Optional<BigDecimal> sl;
	public Optional<BigDecimal> sl() {
		return sl;
	}
	
	public PlaceOrderRequest(OrderSpec spec, Optional<String> refId, Optional<String> externalTradeId, Optional<BigDecimal> tp, Optional<BigDecimal> sl) {
		this.spec = spec;
		this.refId = refId;
		this.externalTradeId = externalTradeId;
		this.tp = tp;
		this.sl = sl;
	}
}