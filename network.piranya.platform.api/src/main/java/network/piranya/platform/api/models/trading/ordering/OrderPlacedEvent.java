package network.piranya.platform.api.models.trading.ordering;

import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.api.models.bots.BotRef;
import network.piranya.platform.api.models.trading.TradingEvent;

public class OrderPlacedEvent implements TradingEvent {
	
	public OrderPlacedEvent(PendingOrder order) {
		this.order = order;
	}
	
	private final PendingOrder order;
	public PendingOrder order() { return order; }
	
	@Override
	public String symbol() {
		return order().spec().symbol();
	}
	
	@Override
	public Optional<BotRef> sourceBotRef() {
		return Optional.of(order.creatorBotRef());
	}
	
}
