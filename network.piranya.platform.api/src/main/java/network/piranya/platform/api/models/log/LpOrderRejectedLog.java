package network.piranya.platform.api.models.log;

import network.piranya.platform.api.models.bots.BotRef;
import network.piranya.platform.api.models.trading.ordering.OrderRef;
import network.piranya.platform.api.models.trading.ordering.OrderRef.PlatformOrderRef;

public class LpOrderRejectedLog extends ActivityLog {
	
	private final PlatformOrderRef orderRef;
	public PlatformOrderRef getOrderRef() {
		return orderRef;
	}
	
	private final String errorMessage;
	public String getErrorMessage() {
		return errorMessage;
	}
	
	private final String liquidityProviderId;
	public String getLiquidityProviderId() {
		return liquidityProviderId;
	}
	
	private final BotRef creatorBotRef;
	public BotRef getCreatorBotRef() {
		return creatorBotRef;
	}
	
	public LpOrderRejectedLog(long time, OrderRef.PlatformOrderRef orderRef, String errorMessage, String liquidityProviderId, BotRef creatorBotRef) {
		super(time);
		this.orderRef = orderRef;
		this.errorMessage = errorMessage;
		this.liquidityProviderId = liquidityProviderId;
		this.creatorBotRef = creatorBotRef;
	}
	
}
