package network.piranya.platform.node.core.execution.engine.activity_log;

import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.api.models.bots.BotRef;
import network.piranya.platform.api.models.trading.ordering.OrderRef;
import network.piranya.platform.api.models.trading.ordering.OrderRef.PlatformOrderRef;

public class OrderPlacementReplyLog implements EsLog {
	
	private final PlatformOrderRef orderRef;
	public PlatformOrderRef orderRef() {
		return orderRef;
	}
	
	private final BotRef creatorBotRef;
	public BotRef creatorBotRef() { return creatorBotRef; }
	
	private final String liquidityProviderId;
	public String liquidityProviderId() {
		return liquidityProviderId;
	}
	
	private final boolean isSuccessful;
	public boolean isSuccessful() {
		return isSuccessful;
	}
	
	private final Optional<String> externalOrderId;
	public Optional<String> externalOrderId() {
		return externalOrderId;
	}
	
	private final Optional<String> errorMessage;
	public Optional<String> errorMessage() {
		return errorMessage;
	}
	
	private final long time;
	public long time() {
		return time;
	}
	
	public OrderPlacementReplyLog(OrderRef.PlatformOrderRef orderRef, BotRef creatorBotRef, String liquidityProviderId,
			boolean isSuccessful, Optional<String> externalOrderId, Optional<String> errorMessage, long time) {
		this.orderRef = orderRef;
		this.creatorBotRef = creatorBotRef;
		this.liquidityProviderId = liquidityProviderId;
		this.isSuccessful = isSuccessful;
		this.externalOrderId = externalOrderId;
		this.errorMessage = errorMessage;
		this.time = time;
	}
	
}
