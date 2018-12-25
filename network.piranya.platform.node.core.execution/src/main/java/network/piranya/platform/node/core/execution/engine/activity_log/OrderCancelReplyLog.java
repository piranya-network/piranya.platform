package network.piranya.platform.node.core.execution.engine.activity_log;

import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.api.models.trading.ordering.OrderRef;

public class OrderCancelReplyLog implements EsLog {
	
	private final OrderRef orderRef;
	public OrderRef orderRef() {
		return orderRef;
	}
	
	private final String liquidityProviderId;
	public String liquidityProviderId() {
		return liquidityProviderId;
	}
	
	private final boolean isSuccessful;
	public boolean isSuccessful() {
		return isSuccessful;
	}
	
	private final Optional<String> errorMessage;
	public Optional<String> errorMessage() {
		return errorMessage;
	}
	
	private final long time;
	public long time() {
		return time;
	}
	
	public OrderCancelReplyLog(OrderRef orderRef, String liquidityProviderId,
			boolean isSuccessful, Optional<String> errorMessage, long time) {
		this.orderRef = orderRef;
		this.liquidityProviderId = liquidityProviderId;
		this.isSuccessful = isSuccessful;
		this.errorMessage = errorMessage;
		this.time = time;
	}
	
}
