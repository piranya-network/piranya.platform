package network.piranya.platform.api.models.trading.ordering;

import network.piranya.platform.api.models.bots.BotRef;

public class PendingOrder {
	
	private final OrderRef orderRef;
	public OrderRef orderRef() {
		return orderRef;
	}
	
	private final OrderSpec spec;
	public OrderSpec spec() {
		return spec;
	}
	
	private final BotRef creatorBotRef;
	public BotRef creatorBotRef() { return creatorBotRef; }
	
	private final OrderRef.ExternalOrderRef externalOrderRef;
	public OrderRef.ExternalOrderRef externalOrderRef() {
		return externalOrderRef;
	}
	
	private final OrderDescription description;
	public OrderDescription description() {
		return description;
	}
	
	private final long createAt;
	public long createAt() {
		return createAt;
	}
	
	private final OrderingProgress progress;
	public OrderingProgress progress() {
		return progress;
	}
	
	private final boolean detached;
	public boolean isDetached() {
		return detached;
	}
	
	public PendingOrder(OrderRef orderRef, BotRef creatorBotRef, OrderSpec spec, OrderRef.ExternalOrderRef externalOrderRef,
			OrderDescription description, long createAt, OrderingProgress progress) {
		this.orderRef = orderRef;
		this.creatorBotRef = creatorBotRef;
		this.spec = spec;
		this.externalOrderRef = externalOrderRef;
		this.description = description;
		this.createAt = createAt;
		this.progress = progress;
		this.detached = orderRef().equals(externalOrderRef());
	}
	
}
