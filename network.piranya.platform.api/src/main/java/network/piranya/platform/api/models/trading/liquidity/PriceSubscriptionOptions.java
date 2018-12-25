package network.piranya.platform.api.models.trading.liquidity;

public class PriceSubscriptionOptions {
	
	private final boolean isSubscribeToOrderBook;
	public boolean isSubscribeToOrderBook() {
		return isSubscribeToOrderBook;
	}
	
	private final boolean isSubscribeToExchangeTrades;
	public boolean isSubscribeToExchangeTrades() {
		return isSubscribeToExchangeTrades;
	}
	
	public PriceSubscriptionOptions(boolean isSubscribeToOrderBook, boolean isSubscribeToExchangeTrades) {
		this.isSubscribeToOrderBook = isSubscribeToOrderBook;
		this.isSubscribeToExchangeTrades = isSubscribeToExchangeTrades;
	}
	
	public PriceSubscriptionOptions() {
		this(false, false);
	}
	
}
