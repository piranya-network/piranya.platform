package network.piranya.platform.node.core.execution.performance;

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
	public void test_place_replay() throws Exception {
		File dataDir = createTempDir();
		EngineSupportWithLp<LiquidityProviderMock> support1 = new EngineSupportWithLp<>(dataDir, LiquidityProviderMock.class, OrderingBot.class);
		support1.executionEngine.init();
		support1.lp.autoAcceptPendingOrders(true);
		
		RefImpl<BotRef> actorRef = new RefImpl<>();
		support1.executionEngine.execute(CreateBot.createByType(OrderingBot.class.getName(), newOrderParams(), Optional.empty(), newBotRefHandler(actorRef)));
		assertNotNull(support1.botsRegistry.get(actorRef.get()));
		support1.botsRegistry.get(actorRef.get());
		
		long t1 = System.currentTimeMillis();
		support1.executionEngine.execute(new InvokeExecutionBotCommand(actorRef.get(), "ENTER", new ParametersBuilder().build(), view -> {}));
		Thread.sleep(500);
		System.out.println("dt: " + (OrderingBot.placedTime - t1));
		
		t1 = System.currentTimeMillis();
		support1.executionEngine.execute(new InvokeExecutionBotCommand(actorRef.get(), "ENTER", new ParametersBuilder().build(), view -> {}));
		Thread.sleep(500);
		System.out.println("dt: " + (OrderingBot.placedTime - t1));
		
		t1 = System.currentTimeMillis();
		support1.executionEngine.execute(new InvokeExecutionBotCommand(actorRef.get(), "ENTER", new ParametersBuilder().build(), view -> {}));
		Thread.sleep(500);
		System.out.println("dt: " + (OrderingBot.placedTime - t1));
		
		//support1.lp.acceptPendingOrders();
		//assertSame(1, support1.executionEngine.tradingState().openOrders().size());
		support1.dispose();
	}
	
	
	protected ParametersBuilder newOrderParamsBuilder() {
		return new ParametersBuilder().string("lp", "LP0").string("symbol", "S/B").bool("is_buy", true).decimal("price", new BigDecimal("1200")).decimal("size", new BigDecimal("1.0"));
	}
	
	protected Parameters newOrderParams() {
		return newOrderParamsBuilder().build();
	}

}
