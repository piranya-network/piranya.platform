package network.piranya.platform.api.models.trading.filling;

import java.math.BigDecimal;

import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.api.models.bots.BotRef;
import network.piranya.platform.api.models.trading.Instrument;
import network.piranya.platform.api.models.trading.TradingEvent;
import network.piranya.platform.api.models.trading.ordering.PendingOrder;

public class FillEvent implements TradingEvent {
	
	private final String id;
	public String id() {
		return id;
	}
	
	private final String tradeId;
	public String tradeId() { return tradeId; }
	
	private final Instrument instrument;
	public Instrument instrument() {
		return instrument;
	}
	
	private final BigDecimal size;
	public BigDecimal size() {
		return size;
	}
	
	private final BigDecimal price;
	public BigDecimal price() {
		return price;
	}
	
	private final long time;
	public long time() {
		return time;
	}
	
	private final PendingOrder order;
	public PendingOrder order() {
		return order;
	}
	
	private final String externalFillId;
	public String externalFillId() {
		return externalFillId;
	}
	
	private final String externalTradeId;
	public String externalTradeId() {
		return externalTradeId;
	}
	
	private final String externalOrderId;
	public String externalOrderId() {
		return externalOrderId;
	}
	
	public FillEvent(String id, String tradeId, Instrument instrument, BigDecimal size, BigDecimal price, long time, PendingOrder order,
			String externalFillId, String externalTradeId, String externalOrderId) {
		this.id = id;
		this.tradeId = tradeId;
		this.instrument = instrument;
		this.size = size;
		this.price = price;
		this.time = time;
		this.order = order;
		this.externalFillId = externalFillId;
		this.externalTradeId = externalTradeId;
		this.externalOrderId = externalOrderId;
	}
	
	@Override
	public String symbol() {
		return instrument().symbol();
	}
	
	@Override
	public Optional<BotRef> sourceBotRef() {
		return Optional.of(order.creatorBotRef());
	}
	
}
