package network.piranya.platform.node.core.execution.lp;

import static org.junit.Assert.*;

import java.io.File;
import java.math.BigDecimal;

import org.junit.Test;

import network.piranya.platform.api.extension_models.Parameters;
import network.piranya.platform.api.extension_models.ParametersBuilder;
import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.api.models.bots.BotRef;
import network.piranya.platform.node.api.execution.commands.CreateBot;
import network.piranya.platform.node.core.execution.testing.support.AbstractExecutionTest;
import network.piranya.platform.node.core.execution.testing.support.EngineSupportWithLp;
import network.piranya.platform.node.core.execution.testing.support.LiquidityProviderMock;
import network.piranya.platform.node.core.execution.trading.LimitOrderBot;
import network.piranya.platform.node.core.execution.trading.MarketOrderBot;
import network.piranya.platform.node.utilities.RefImpl;

public class LpBasicTests extends AbstractExecutionTest {
	
	@Test
	public void test_removed_lp_replay() {
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
		
		/// LP instructed not to be registered. Must replay normally.
		EngineSupportWithLp<LiquidityProviderMock> support2 = new EngineSupportWithLp<>(dataDir, LiquidityProviderMock.class, false, LimitOrderBot.class, MarketOrderBot.class);
		support2.executionEngine.init();
		assertNotNull(support2.botsRegistry.get(actorRef.get()));
		assertSame(1, support2.executionEngine.tradingState().openOrders().orders().size());
		
		// TODO
		/// After replay though, orders must not be accepted
		//RefImpl<BotRef> actorRef2 = new RefImpl<>();
		//support2.executionEngine.execute(CreateBot.createByType(LimitOrderBot.class.getName(), newOrderParams(), Optional.empty(), newBotRefHandler(actorRef2)));
		//assertNotNull(support1.botsRegistry.get(actorRef.get()));
	}
	
	
	protected ParametersBuilder newOrderParamsBuilder() {
		return new ParametersBuilder().string("lp", "LP0").string("symbol", "S/B").bool("is_buy", true).decimal("price", new BigDecimal("1200")).decimal("size", new BigDecimal("1.0"));
	}
	
	protected Parameters newOrderParams() {
		return newOrderParamsBuilder().build();
	}

}
