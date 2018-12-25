package network.piranya.platform.node.core.execution.trading;

import static org.junit.Assert.*;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import network.piranya.platform.api.exceptions.BotNotFoundException;
import network.piranya.platform.api.extension_models.Parameters;
import network.piranya.platform.api.extension_models.ParametersBuilder;
import network.piranya.platform.api.extension_models.execution.bots.BotEvent;
import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.api.models.bots.BotRef;
import network.piranya.platform.api.models.bots.BotView;
import network.piranya.platform.node.api.execution.commands.InvokeExecutionBotCommand;
import network.piranya.platform.node.api.execution.commands.CreateBot;
import network.piranya.platform.node.core.execution.testing.support.AbstractExecutionTest;
import network.piranya.platform.node.core.execution.testing.support.EngineSupportWithLp;
import network.piranya.platform.node.core.execution.testing.support.LiquidityProviderMock;
import network.piranya.platform.node.utilities.RefImpl;

public class OrderingBasicTests extends AbstractExecutionTest {
	
	@Test
	public void test_place_replay() {
		File dataDir = createTempDir();
		EngineSupportWithLp<LiquidityProviderMock> support1 = new EngineSupportWithLp<>(dataDir, LiquidityProviderMock.class, LimitOrderBot.class, MarketOrderBot.class);
		support1.executionEngine.init();
		
		RefImpl<BotRef> actorRef = new RefImpl<>();
		support1.executionEngine.execute(CreateBot.createByType(LimitOrderBot.class.getName(), newOrderParams(), Optional.empty(), newBotRefHandler(actorRef)));
		assertNotNull(support1.botsRegistry.get(actorRef.get()));
		support1.botsRegistry.get(actorRef.get());
		waitUntil(100, () -> support1.lp.pendingOrdersList().size() > 0);
		assertSame(1, support1.lp.pendingOrdersList().size());
		assertSame(0, support1.executionEngine.tradingState().openOrders().orders().size());
		
		support1.lp.acceptPendingOrders();
		assertSame(1, support1.executionEngine.tradingState().openOrders().orders().size());
		support1.dispose();
		
		EngineSupportWithLp<LiquidityProviderMock> support2 = new EngineSupportWithLp<>(dataDir, LiquidityProviderMock.class, LimitOrderBot.class, MarketOrderBot.class);
		support2.executionEngine.init();
		assertNotNull(support2.botsRegistry.get(actorRef.get()));
		assertSame(1, support2.executionEngine.tradingState().openOrders().orders().size());
		assertSame(0, support2.lp.pendingOrdersList().size()); /// order will not be sent to LP since it's confirmed that it was accepted
	}
	
	@Test
	public void test_place_cancel_replay() {
		File dataDir = createTempDir();
		EngineSupportWithLp<LiquidityProviderMock> support1 = new EngineSupportWithLp<>(dataDir, LiquidityProviderMock.class, LimitOrderBot.class, MarketOrderBot.class);
		support1.executionEngine.init();
		
		RefImpl<BotRef> actorRef = new RefImpl<>();
		support1.executionEngine.execute(CreateBot.createByType(LimitOrderBot.class.getName(), newOrderParams(), Optional.empty(), newBotRefHandler(actorRef)));
		assertNotNull(support1.botsRegistry.get(actorRef.get()));
		waitUntil(100, () -> support1.lp.pendingOrdersList().size() > 0);
		support1.botsRegistry.get(actorRef.get());
		assertSame(1, support1.lp.pendingOrdersList().size());
		support1.lp.acceptPendingOrders();
		assertSame(1, support1.executionEngine.tradingState().openOrders().orders().size());
		
		support1.executionEngine.execute(new InvokeExecutionBotCommand(actorRef.get(), "CANCEL", new ParametersBuilder().build(), view -> {}));
		support1.lp.acceptCancelOrders();
		assertSame(0, support1.executionEngine.tradingState().openOrders().orders().size());
		support1.dispose();
		
		EngineSupportWithLp<LiquidityProviderMock> support2 = new EngineSupportWithLp<>(dataDir, LiquidityProviderMock.class, LimitOrderBot.class, MarketOrderBot.class);
		support2.executionEngine.init();
		assertSame(0, support1.executionEngine.tradingState().openOrders().orders().size());
	}
	
	@Test(expected = BotNotFoundException.class)
	public void test_place_fill_replay() {
		File dataDir = createTempDir();
		EngineSupportWithLp<LiquidityProviderMock> support1 = new EngineSupportWithLp<>(dataDir, LiquidityProviderMock.class, LimitOrderBot.class, MarketOrderBot.class);
		support1.executionEngine.init();
		
		RefImpl<BotRef> actorRef = new RefImpl<>();
		support1.executionEngine.execute(CreateBot.createByType(LimitOrderBot.class.getName(), newOrderParams(), Optional.empty(), newBotRefHandler(actorRef)));
		assertNotNull(support1.botsRegistry.get(actorRef.get()));
		support1.botsRegistry.get(actorRef.get());
		waitUntil(100, () -> support1.lp.pendingOrdersList().size() > 0);
		assertSame(1, support1.lp.pendingOrdersList().size());
		
		support1.lp.acceptPendingOrders();
		assertSame(1, support1.executionEngine.tradingState().openOrders().orders().size());
		support1.lp.fillPendingOrders();
		assertSame(0, support1.executionEngine.tradingState().openOrders().orders().size());
		assertSame(1, support1.executionEngine.tradingState().openTrades().trades().size());
		assertSame(1, support1.executionEngine.tradingState().openTrades().trades().get(0).fills().size());
		support1.dispose();
		
		EngineSupportWithLp<LiquidityProviderMock> support2 = new EngineSupportWithLp<>(dataDir, LiquidityProviderMock.class, LimitOrderBot.class, MarketOrderBot.class);
		support2.executionEngine.init();
		assertSame(0, support2.executionEngine.tradingState().openOrders().orders().size());
		assertSame(1, support2.executionEngine.tradingState().openTrades().trades().size());
		assertSame(1, support2.executionEngine.tradingState().openTrades().trades().get(0).fills().size());
		support2.botsRegistry.get(actorRef.get());
	}
	
	@Test
	public void test_place_fill_internal_limit_order() {
		File dataDir = createTempDir();
		EngineSupportWithLp<LiquidityProviderMock> support1 = new EngineSupportWithLp<>(dataDir, LiquidityProviderMock.class, InternallyHeldLimitOrderBot.class);
		support1.executionEngine.init();
		//support1.lp.marketOrderHandler((request, handler) -> handler.accept(new Result<>(new PlaceOrderReply("EX0"))));
		
		RefImpl<BotView> agentView = new RefImpl<>();
		support1.executionEngine.execute(CreateBot.createByType(InternallyHeldLimitOrderBot.class.getName(),
				newOrderParamsBuilder().decimal("price", new BigDecimal("1200")).build(), Optional.empty(), newBotViewHandler(agentView)));
		RefImpl<BotRef> agentRef = new RefImpl<>();
		agentRef.set(agentView.get().ref());
		assertNotNull(support1.botsRegistry.get(agentRef.get()));
		support1.botsRegistry.get(agentRef.get());
		assertSame(0, support1.lp.marketOrdersList().size());
		assertSame(0, support1.executionEngine.tradingState().openOrders().orders().size());
		
		List<BotEvent> agentEvents = new ArrayList<>();
		agentView.get().subscribe(event -> agentEvents.add(event));
		support1.lp.publishQuote("S/B", new BigDecimal("1190"), new BigDecimal("1200"));
		assertSame(1, agentEvents.size());
		assertEquals("TRIGGERED", agentEvents.get(0).eventTypeId());
		support1.lp.acceptMarketOrders();
		assertSame(1, support1.executionEngine.tradingState().openOrders().orders().size());
		assertSame(2, agentEvents.size());
		assertEquals("PLACED", agentEvents.get(1).eventTypeId());
		support1.dispose();
		
		EngineSupportWithLp<LiquidityProviderMock> support2 = new EngineSupportWithLp<>(dataDir, LiquidityProviderMock.class, InternallyHeldLimitOrderBot.class);
		support2.executionEngine.init();
		assertNotNull(support2.botsRegistry.get(agentRef.get()));
		assertSame(1, support2.executionEngine.tradingState().openOrders().orders().size());
	}
	
	//@Test
	public void test_place_fill_partially_replay() {
		fail("Test Empty");
	}
	
	
	protected ParametersBuilder newOrderParamsBuilder() {
		return new ParametersBuilder().string("lp", "LP0").string("symbol", "S/B").bool("is_buy", true).decimal("price", new BigDecimal("1200")).decimal("size", new BigDecimal("1.0"));
	}
	
	protected Parameters newOrderParams() {
		return newOrderParamsBuilder().build();
	}

}
