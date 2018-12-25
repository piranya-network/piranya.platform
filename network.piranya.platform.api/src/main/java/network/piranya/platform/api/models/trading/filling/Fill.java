package network.piranya.platform.api.models.trading.filling;

import java.math.BigDecimal;

import network.piranya.platform.api.models.trading.ordering.PendingOrder;

public class Fill {
	
	private final String fillId;
	public String fillId() { return fillId; }
	
	private final String tradeId;
	public String tradeId() { return tradeId; }
	
	private final String externalFillId;
	public String externalFillId() { return externalFillId; }
	
	private final String externalTradeId;
	public String externalTradeId() { return externalTradeId; }
	
	private final String liquidityProviderId;
	public String liquidityProviderId() { return liquidityProviderId; }
	
	private final String symbol;
	public String symbol() { return symbol; }
	
	private final BigDecimal price;
	public BigDecimal price() { return price; }
	
	private final BigDecimal size;
	public BigDecimal size() { return size; }
	
	private final Fee[] fees;
	public Fee[] fees() { return fees; }
	
	private final long time;
	public long time() { return time; }
	
	private final PendingOrder order;
	public PendingOrder order() { return order; }
	
	public Fill(String fillId, String tradeId, String externalFillId, String externalTradeId, String liquidityProviderId,
			String symbol, BigDecimal price, BigDecimal size, Fee[] fees, long time, PendingOrder order) {
		this.fillId = fillId;
		this.tradeId = tradeId;
		this.externalFillId = externalFillId;
		this.externalTradeId = externalTradeId;
		this.liquidityProviderId = liquidityProviderId;
		this.symbol = symbol;
		this.price = price;
		this.size = size;
		this.fees = fees;
		this.time = time;
		this.order = order;
	}
	
}
