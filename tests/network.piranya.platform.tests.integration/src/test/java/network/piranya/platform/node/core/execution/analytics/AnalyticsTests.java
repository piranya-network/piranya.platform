package network.piranya.platform.node.core.execution.analytics;

import static org.junit.Assert.*;

import java.io.File;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import network.piranya.platform.api.extension_models.Parameters;
import network.piranya.platform.api.extension_models.ParametersBuilder;
import network.piranya.platform.api.lang.OpenEndedPeriod;
import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.api.lang.Period;
import network.piranya.platform.api.lang.Result;
import network.piranya.platform.api.models.analytics.AnalyticalViewData;
import network.piranya.platform.api.models.bots.BotRef;
import network.piranya.platform.node.api.execution.commands.CreateBot;
import network.piranya.platform.node.core.execution.testing.support.AbstractExecutionTest;
import network.piranya.platform.node.core.execution.testing.support.EngineSupportWithLp;
import network.piranya.platform.node.core.execution.testing.support.LiquidityProviderMock;
import network.piranya.platform.node.core.execution.trading.LimitOrderBot;
import network.piranya.platform.node.core.execution.trading.MarketOrderBot;
import network.piranya.platform.node.utilities.RefImpl;

public class AnalyticsTests extends AbstractExecutionTest {
	
	@Test
	public void test_basic_fill_analysis() {
		File dataDir = createTempDir();
		EngineSupportWithLp<LiquidityProviderMock> support1 = new EngineSupportWithLp<>(dataDir, LiquidityProviderMock.class, LimitOrderBot.class, MarketOrderBot.class);
		support1.executionEngine.analyticsEngine().registerViewType(FillsStatsAnalyticalView.class);
		support1.executionEngine.init();
		support1.executionEngine.analyticsEngine().init();
		
		RefImpl<AnalyticalViewData<FillsStats>> viewRef = new RefImpl<>();
		support1.executionEngine.analyticsEngine().view("view1", FillsStatsAnalyticalView.class.getName(), false, OpenEndedPeriod.fixedStartTime(0), params().build(),
				(Result<AnalyticalViewData<FillsStats>> result) -> viewRef.set(result.result().get()));
		assertTrue(waitUntil(100, () -> viewRef.get() != null));
		
		AtomicInteger viewUpdatesCounter = new AtomicInteger(0);
		viewRef.get().subscribe(data -> viewUpdatesCounter.incrementAndGet());
		
		RefImpl<BotRef> botRef = new RefImpl<>();
		support1.executionEngine.execute(CreateBot.createByType(LimitOrderBot.class.getName(), newOrderParams(), Optional.empty(), newBotRefHandler(botRef)));
		support1.botsRegistry.get(botRef.get());
		waitUntil(100, () -> support1.lp.pendingOrdersList().size() > 0);
		assertSame(1, support1.lp.pendingOrdersList().size());
		
		support1.lp.acceptPendingOrders();
		assertSame(1, support1.executionEngine.tradingState().openOrders().orders().size());
		support1.lp.fillPendingOrders();
		assertSame(1, support1.executionEngine.tradingState().openTrades().trades().size());
		
		assertTrue(waitUntil(100, () -> viewUpdatesCounter.get() == 1));
		assertEquals(1, viewRef.get().data().getCount());
		assertEquals(new BigDecimal("1.0"), viewRef.get().data().getTotalSize());
		
		/*
		support1.executionEngine.execute(CreateBot.createByType(LimitOrderBot.class.getName(), newOrderParams(), Optional.empty(), newBotRefHandler(actorRef)));
		support1.lp.acceptPendingOrders();
		support1.lp.fillPendingOrders();
		assertTrue(waitUntil(100, () -> viewUpdatesCounter.get() == 2));
		assertEquals(2, viewRef.get().data().getCount());
		assertEquals(new BigDecimal("2.0"), viewRef.get().data().getTotalSize());
		*/
	}
	
	@Test
	public void test_basic_fill_query() {
		File dataDir = createTempDir();
		EngineSupportWithLp<LiquidityProviderMock> support1 = new EngineSupportWithLp<>(dataDir, LiquidityProviderMock.class, LimitOrderBot.class, MarketOrderBot.class);
		support1.executionEngine.analyticsEngine().registerQueryType(FillsStatsAnalyticalView.class);
		support1.executionEngine.init();
		support1.executionEngine.analyticsEngine().init();
		
		RefImpl<BotRef> botRef = new RefImpl<>();
		support1.executionEngine.execute(CreateBot.createByType(LimitOrderBot.class.getName(), newOrderParams(), Optional.empty(), newBotRefHandler(botRef)));
		support1.botsRegistry.get(botRef.get());
		waitUntil(100, () -> support1.lp.pendingOrdersList().size() > 0);
		assertSame(1, support1.lp.pendingOrdersList().size());
		
		support1.lp.acceptPendingOrders();
		assertSame(1, support1.executionEngine.tradingState().openOrders().orders().size());
		support1.lp.fillPendingOrders();
		assertSame(1, support1.executionEngine.tradingState().openTrades().trades().size());
		
		int[] countResult = new int[] { -1 };
		support1.executionEngine.analyticsEngine().query(FillsStatsAnalyticalView.class.getName(), Period.between(0L, Long.MAX_VALUE), params().build(),
				(Result<FillsStats> result) -> countResult[0] = result.result().get().getCount());
		assertTrue(waitUntil(500, () -> countResult[0] != -1));
		assertEquals(1, countResult[0]);
	}
	
	
	protected ParametersBuilder newOrderParamsBuilder() {
		return new ParametersBuilder().string("lp", "LP0").string("symbol", "S/B").bool("is_buy", true).decimal("price", new BigDecimal("1200")).decimal("size", new BigDecimal("1.0"));
	}
	
	protected Parameters newOrderParams() {
		return newOrderParamsBuilder().build();
	}

}
