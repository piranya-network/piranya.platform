package network.piranya.platform.api.models.trading.liquidity;

import java.math.BigDecimal;
import network.piranya.platform.api.lang.Optional;

import network.piranya.platform.api.lang.None;
import network.piranya.platform.api.lang.WithinContextReplyHandler;
import network.piranya.platform.api.models.trading.Instrument;
import network.piranya.platform.api.models.trading.Quote;
import network.piranya.platform.api.models.trading.ordering.OrderDescription;
import network.piranya.platform.api.models.trading.ordering.OrderRef;
import network.piranya.platform.api.models.trading.ordering.OrderSpec;

public interface LiquidityProvider<Context> {
	
	WithinContextReplyHandler<OrderRef, Context> placeOrder(OrderSpec spec, Optional<OrderDescription> description);
	WithinContextReplyHandler<OrderRef, Context> placeOrder(OrderSpec spec);
	
	WithinContextReplyHandler<OrderRef, Context> buy(String symbol, BigDecimal orderSize);
	WithinContextReplyHandler<OrderRef, Context> sell(String symbol, BigDecimal orderSize);
	WithinContextReplyHandler<OrderRef, Context> buyLimit(String symbol, BigDecimal orderSize, BigDecimal price);
	WithinContextReplyHandler<OrderRef, Context> sellLimit(String symbol, BigDecimal orderSize, BigDecimal price);
	WithinContextReplyHandler<OrderRef, Context> buyStop(String symbol, BigDecimal orderSize, BigDecimal price);
	WithinContextReplyHandler<OrderRef, Context> sellStop(String symbol, BigDecimal orderSize, BigDecimal price);
	
	
	WithinContextReplyHandler<None, Context> cancelOrder(OrderRef orderRef);
	
	Optional<Quote> quote(Instrument instrument);
	
	
	void subscribeToPrices(String... symbols);
	void unsubscribeFromPrices(String... symbols);
	void subscribeToPrices(PriceSubscriptionOptions options, String... symbols);
	void unsubscribeFromPrices(PriceSubscriptionOptions options, String... symbols);
	
}
