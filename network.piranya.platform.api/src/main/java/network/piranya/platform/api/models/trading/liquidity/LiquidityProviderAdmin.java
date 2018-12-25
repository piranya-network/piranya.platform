package network.piranya.platform.api.models.trading.liquidity;

import java.util.function.Consumer;

import network.piranya.platform.api.extension_models.execution.liquidity.LpEvent;
import network.piranya.platform.api.lang.None;
import network.piranya.platform.api.lang.ResultHandler;
import network.piranya.platform.api.models.metadata.LiquidityProviderInfo;

public interface LiquidityProviderAdmin {
	
	LiquidityProviderInfo info();
	
	LimitedAccess access();
	
	void connect(ResultHandler<None> handler);
	void disconnect(ResultHandler<None> handler);
	
	
	public interface LimitedAccess {
		
		void subscribe(Consumer<LpEvent> subscriber);
		
		void subscribeToPrices(String... symbols);
		void unsubscribeFromPrices(String... symbols);
		void subscribeToPrices(PriceSubscriptionOptions options, String... symbols);
		void unsubscribeFromPrices(PriceSubscriptionOptions options, String... symbols);
		
		LpActivityInfo activityInfo();
	}
	
}
