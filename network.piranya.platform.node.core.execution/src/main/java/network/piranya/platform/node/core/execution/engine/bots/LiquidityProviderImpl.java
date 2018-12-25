package network.piranya.platform.node.core.execution.engine.bots;

import static network.piranya.platform.node.utilities.CollectionUtils.find;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import network.piranya.platform.api.extension_models.execution.bots.ExecutionBot;
import network.piranya.platform.api.extension_models.execution.bots.ExecutionBot.ExecutionContext;
import network.piranya.platform.api.lang.None;
import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.api.lang.WithinContextReplyHandler;
import network.piranya.platform.api.models.trading.Instrument;
import network.piranya.platform.api.models.trading.Quote;
import network.piranya.platform.api.models.trading.liquidity.LiquidityProvider;
import network.piranya.platform.api.models.trading.liquidity.PriceSubscriptionOptions;
import network.piranya.platform.api.models.trading.liquidity.QuoteEvent;
import network.piranya.platform.api.models.trading.ordering.OrderDescription;
import network.piranya.platform.api.models.trading.ordering.OrderRef;
import network.piranya.platform.api.models.trading.ordering.OrderSpec;
import network.piranya.platform.api.models.trading.ordering.OrderType;
import network.piranya.platform.node.core.execution.engine.LpExectuionBranch;

public class LiquidityProviderImpl implements LiquidityProvider<ExecutionBot.ExecutionContext> {
	
	@Override
	public WithinContextReplyHandler<OrderRef, ExecutionBot.ExecutionContext> placeOrder(OrderSpec spec, Optional<OrderDescription> description) {
		context().checkIfDisposed();
		
		return lpExectuionBranch().placeOrder(new OrderRef.PlatformOrderRef(bot().nextOrderId()), spec, bot().bot().ref(), description, executionContextSupplier());
	}
	
	@Override
	public WithinContextReplyHandler<OrderRef, ExecutionBot.ExecutionContext> placeOrder(OrderSpec spec) {
		return placeOrder(spec, Optional.empty());
	}
	
	@Override
	public WithinContextReplyHandler<OrderRef, ExecutionBot.ExecutionContext> buy(String symbol, BigDecimal orderSize) {
		return placeOrder(new OrderSpec(symbol, true, orderSize), Optional.empty());
	}
	
	@Override
	public WithinContextReplyHandler<OrderRef, ExecutionBot.ExecutionContext> sell(String symbol, BigDecimal orderSize) {
		return placeOrder(new OrderSpec(symbol, false, orderSize), Optional.empty());
	}
	
	@Override
	public WithinContextReplyHandler<OrderRef, ExecutionBot.ExecutionContext> buyLimit(String symbol, BigDecimal orderSize, BigDecimal price) {
		return placeOrder(new OrderSpec(symbol, OrderType.LIMIT, true, price, orderSize), Optional.empty());
	}
	
	@Override
	public WithinContextReplyHandler<OrderRef, ExecutionBot.ExecutionContext> sellLimit(String symbol, BigDecimal orderSize, BigDecimal price) {
		return placeOrder(new OrderSpec(symbol, OrderType.LIMIT, false, price, orderSize), Optional.empty());
	}
	
	@Override
	public WithinContextReplyHandler<OrderRef, ExecutionBot.ExecutionContext> buyStop(String symbol, BigDecimal orderSize, BigDecimal price) {
		return placeOrder(new OrderSpec(symbol, OrderType.STOP, true, price, orderSize), Optional.empty());
	}
	
	@Override
	public WithinContextReplyHandler<OrderRef, ExecutionBot.ExecutionContext> sellStop(String symbol, BigDecimal orderSize, BigDecimal price) {
		return placeOrder(new OrderSpec(symbol, OrderType.STOP, false, price, orderSize), Optional.empty());
	}
	
	@Override
	public WithinContextReplyHandler<None, ExecutionBot.ExecutionContext> cancelOrder(OrderRef orderRef) {
		context().checkIfDisposed();
		
		return lpExectuionBranch().cancelOrder(orderRef, executionContextSupplier());
	}
	
	@Override
	public Optional<Quote> quote(Instrument instrument) {
		return lpExectuionBranch().quote(instrument);
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
	public void subscribeToPrices(PriceSubscriptionOptions options, String... symbols) {
		context().checkIfDisposed();
		
		lpExectuionBranch().subscribeToPrices(options, symbols);
		Set<String> symbolsSet = new HashSet<>(Arrays.asList(symbols));
		context().subscribe(QuoteEvent.class, q -> lpExectuionBranch.liquidityProviderId().equals(q.instrument().sourceId())
				&& find(symbolsSet, s -> q.instrument().matches(s)).isPresent());
	}
	
	@Override
	public void unsubscribeFromPrices(PriceSubscriptionOptions options, String... symbols) {
		context().checkIfDisposed();
		
		lpExectuionBranch().unsubscribeFromPrices(options, symbols);
	}
	
	
	public LiquidityProviderImpl(LpExectuionBranch lpExectuionBranch, RunningBot bot, AbstractBotContext context, Supplier<ExecutionBot.ExecutionContext> executionContextSupplier) {
		this.lpExectuionBranch = lpExectuionBranch;
		this.bot = bot;
		this.context = context;
		this.executionContextSupplier = executionContextSupplier;
	}
	
	private final LpExectuionBranch lpExectuionBranch;
	protected LpExectuionBranch lpExectuionBranch() {
		return lpExectuionBranch;
	}
	
	private final RunningBot bot;
	protected RunningBot bot() {
		return bot;
	}
	
	private final AbstractBotContext context;
	protected AbstractBotContext context() {
		return context;
	}
	
	private final Supplier<ExecutionContext> executionContextSupplier;
	protected Supplier<ExecutionContext> executionContextSupplier() {
		return executionContextSupplier;
	}
	
}
