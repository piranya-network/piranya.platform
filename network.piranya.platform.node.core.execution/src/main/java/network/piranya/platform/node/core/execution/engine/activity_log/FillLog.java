package network.piranya.platform.node.core.execution.engine.activity_log;

import java.math.BigDecimal;

import network.piranya.platform.api.models.trading.filling.Fee;

public class FillLog implements EsLog {
	
	private final String externalFillId;
	public String externalFillId() {
		return externalFillId;
	}
	
	private final String externalOrderId;
	public String externalOrderId() {
		return externalOrderId;
	}
	
	private final String externalTradeId;
	public String externalTradeId() {
		return externalTradeId;
	}
	
	private final String liquidityProviderId;
	public String liquidityProviderId() {
		return liquidityProviderId;
	}
	
	private final String symbol;
	public String symbol() {
		return symbol;
	}
	
	private final BigDecimal price;
	public BigDecimal price() {
		return price;
	}
	
	private final BigDecimal size;
	public BigDecimal size() {
		return size;
	}
	
	private final Fee[] fees;
	public Fee[] fees() { return fees; }
	
	private final long time;
	public long time() {
		return time;
	}
	
	public FillLog(String externalFillId, String externalOrderId, String externalTradeId, String liquidityProviderId,
			String symbol, BigDecimal price, BigDecimal size, Fee[] fees, long time) {
		this.externalFillId = externalFillId;
		this.externalOrderId = externalOrderId;
		this.externalTradeId = externalTradeId;
		this.liquidityProviderId = liquidityProviderId;
		this.symbol = symbol;
		this.price = price;
		this.size = size;
		this.fees = fees;
		this.time = time;
	}
	
}
