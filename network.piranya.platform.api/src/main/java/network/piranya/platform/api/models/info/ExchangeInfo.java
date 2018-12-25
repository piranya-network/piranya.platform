package network.piranya.platform.api.models.info;

public class ExchangeInfo implements Comparable<ExchangeInfo> {
	
	private final String exchangeId;
	public String exchangeId() {
		return exchangeId;
	}
	
	public ExchangeInfo(String exchangeId) {
		this.exchangeId = exchangeId;
	}
	
	@Override
	public int compareTo(ExchangeInfo o) {
		return exchangeId().compareTo(o.exchangeId());
	}
	
}
