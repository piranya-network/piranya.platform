package network.piranya.platform.api.models.log;

import network.piranya.platform.api.lang.Optional;

import network.piranya.platform.api.models.bots.BotRef;
import network.piranya.platform.api.models.trading.Quote;
import network.piranya.platform.api.models.trading.ordering.OrderDescription;
import network.piranya.platform.api.models.trading.ordering.OrderRef;
import network.piranya.platform.api.models.trading.ordering.OrderRef.PlatformOrderRef;
import network.piranya.platform.api.models.trading.ordering.OrderSpec;

public class LpOrderPlaceLog extends ActivityLog {
	
	private final PlatformOrderRef orderRef;
	public PlatformOrderRef getOrderRef() {
		return orderRef;
	}
	
	private final BotRef creatorBotRef;
	public BotRef getCreatorBotRef() {
		return creatorBotRef;
	}
	
	private final OrderSpec spec;
	public OrderSpec getSpec() {
		return spec;
	}
	
	private final String liquidityProviderId;
	public String getLiquidityProviderId() {
		return liquidityProviderId;
	}
	
	private final Optional<OrderDescription> optionalDescription;
	public Optional<OrderDescription> getOptionalDescription() {
		return optionalDescription;
	}
	
	private final Optional<Quote> currentQuote;
	public Optional<Quote> getCurrentQuote() {
		return currentQuote;
	}
	
	public LpOrderPlaceLog(long time, OrderRef.PlatformOrderRef orderRef, BotRef creatorBotRef, OrderSpec spec, String liquidityProviderId,
			Optional<OrderDescription> optionalDescription, Optional<Quote> currentQuote) {
		super(time);
		this.orderRef = orderRef;
		this.creatorBotRef = creatorBotRef;
		this.spec = spec;
		this.liquidityProviderId = liquidityProviderId;
		this.optionalDescription = optionalDescription;
		this.currentQuote = currentQuote;
	}
	
}
