package network.piranya.platform.api.extension_models.execution.liquidity;

import network.piranya.platform.api.lang.Optional;

import network.piranya.platform.api.models.trading.ordering.OrderSpec;

public class LpPendingOrder {
	
	private final OrderSpec spec;
	public OrderSpec getSpec() {
		return spec;
	}
	
	private final Optional<String> refId;
	public Optional<String> getRefId() {
		return refId;
	}
	
	public LpPendingOrder(OrderSpec spec, Optional<String> refId) {
		this.spec = spec;
		this.refId = refId;
	}
	
}
