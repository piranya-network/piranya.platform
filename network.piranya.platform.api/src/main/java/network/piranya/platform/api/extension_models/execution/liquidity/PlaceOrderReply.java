package network.piranya.platform.api.extension_models.execution.liquidity;

public class PlaceOrderReply {
	
	private final String externalOrderId;
	public String externalOrderId() {
		return externalOrderId;
	}
	
	public PlaceOrderReply(String externalOrderId) {
		this.externalOrderId = externalOrderId;
	}
}