package network.piranya.platform.api.extension_models.execution.liquidity;

import java.math.BigDecimal;

import network.piranya.platform.api.models.trading.Instrument;
import network.piranya.platform.api.models.trading.filling.Fee;

public class AcceptFill implements LpEvent {
	
	private final String externalFillId;
	public String externalFillId() { return externalFillId; }
	
	private final String externalOrderId;
	public String externalOrderId() { return externalOrderId; }
	
	private final String externalTradeId;
	public String externalTradeId() { return externalTradeId; }
	
	private final Instrument instrument;
	public Instrument instrument() { return instrument; }
	
	private final BigDecimal price;
	public BigDecimal price() { return price; }
	
	private final BigDecimal size;
	public BigDecimal size() { return size; }
	
	private final Fee[] fees;
	public Fee[] fees() { return fees; }
	
	private final long time;
	public long time() { return time; }
	
	public AcceptFill(String externalFillId, String externalOrderId, String externalTradeId, Instrument instrument, BigDecimal price, BigDecimal size, Fee[] fees, long time) {
		this.externalFillId = externalFillId;
		this.externalOrderId = externalOrderId;
		this.externalTradeId = externalTradeId;
		this.instrument = instrument;
		this.price = price;
		this.size = size;
		this.fees = fees;
		this.time = time;
	}
	
}