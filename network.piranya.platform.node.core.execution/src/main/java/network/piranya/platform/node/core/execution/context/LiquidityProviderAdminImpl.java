package network.piranya.platform.node.core.execution.context;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import network.piranya.platform.api.extension_models.execution.liquidity.LiquidityProvider;
import network.piranya.platform.api.extension_models.execution.liquidity.LpEvent;
import network.piranya.platform.api.lang.None;
import network.piranya.platform.api.lang.ResultHandler;
import network.piranya.platform.api.models.metadata.LiquidityProviderInfo;
import network.piranya.platform.api.models.trading.liquidity.LiquidityProviderAdmin;
import network.piranya.platform.api.models.trading.liquidity.LpActivityInfo;
import network.piranya.platform.api.models.trading.liquidity.PriceSubscriptionOptions;
import network.piranya.platform.node.utilities.CollectionUtils;

public class LiquidityProviderAdminImpl implements LiquidityProviderAdmin {
	
	@Override
	public LiquidityProviderInfo info() {
		return lp().info();
	}
	
	@Override
	public LimitedAccess access() {
		return limitedAccess;
	}
	
	@Override
	public void connect(ResultHandler<None> handler) {
		lp().connect(handler);
	}
	
	@Override
	public void disconnect(ResultHandler<None> handler) {
		lp().disconnect(handler);
	}
	
	public void dispose() {
		limitedAccess.dispose();
	}
	
	
	public LiquidityProviderAdminImpl(LiquidityProvider lp) {
		this.lp = lp;
		this.limitedAccess = new LimitedAccessImpl();
	}
	
	private final LiquidityProvider lp;
	protected LiquidityProvider lp() { return lp; }
	
	private final LimitedAccessImpl limitedAccess;
	
	
	protected class LimitedAccessImpl implements LimitedAccess {
		
		@Override
		public void subscribe(Consumer<LpEvent> subscriber) {
			subscribers.put(subscriber, true);
			lp().subscribe(subscriber);
		}
		
		@Override
		public void subscribeToPrices(PriceSubscriptionOptions options, String... symbols) {
			lp().subscribeToPrices(options, symbols);
		}
		
		@Override
		public void unsubscribeFromPrices(PriceSubscriptionOptions options, String... symbols) {
			lp().unsubscribeFromPrices(options, symbols);
		}
		
		@Override
		public void subscribeToPrices(String... symbols) {
			subscribeToPrices(new PriceSubscriptionOptions(), symbols);
		}
		
		@Override
		public void unsubscribeFromPrices(String... symbols) {
			unsubscribeFromPrices(new PriceSubscriptionOptions(), symbols);
		}
		
		@Override
		public LpActivityInfo activityInfo() {
			return lp().activityInfo();
		}
		
		public void dispose() {
			CollectionUtils.foreach(subscribers.keySet(), subscriber -> lp().unsubscribe(subscriber));
		}
		
		private final ConcurrentMap<Consumer<LpEvent>, Boolean> subscribers = new ConcurrentHashMap<>();
	}
	
}
