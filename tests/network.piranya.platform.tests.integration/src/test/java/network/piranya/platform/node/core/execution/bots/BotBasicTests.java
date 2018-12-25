package network.piranya.platform.node.core.execution.bots;

import static org.junit.Assert.*;

import java.io.File;
import java.math.BigDecimal;

import org.junit.Test;

import network.piranya.platform.api.exceptions.BotNotFoundException;
import network.piranya.platform.api.extension_models.Data;
import network.piranya.platform.api.extension_models.ParametersBuilder;
import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.api.models.bots.BotRef;
import network.piranya.platform.api.models.bots.BotView;
import network.piranya.platform.node.api.execution.commands.AbortBot;
import network.piranya.platform.node.api.execution.commands.InvokeExecutionBotCommand;
import network.piranya.platform.node.api.execution.commands.CreateBot;
import network.piranya.platform.node.core.execution.testing.support.AbstractExecutionTest;
import network.piranya.platform.node.core.execution.testing.support.EngineSupportWithLp;
import network.piranya.platform.node.core.execution.testing.support.LiquidityProviderMock;
import network.piranya.platform.node.utilities.RefImpl;

public class BotBasicTests extends AbstractExecutionTest {
	
	@Test
	public void test_create_replay() {
		File dataDir = createTempDir();
		EngineSupportWithLp<LiquidityProviderMock> support1 = new EngineSupportWithLp<>(dataDir, LiquidityProviderMock.class, BasicBot.class);
		support1.executionEngine.init();
		
		RefImpl<BotRef> actorRef = new RefImpl<>();
		support1.executionEngine.execute(CreateBot.createByType(BasicBot.class.getName(), new ParametersBuilder().bool("finish_on_start", false).build(),
				Optional.empty(), newBotRefHandler(actorRef)));
		assertNotNull(support1.botsRegistry.get(actorRef.get()));
		support1.dispose();
		
		EngineSupportWithLp<LiquidityProviderMock> support2 = new EngineSupportWithLp<>(dataDir, LiquidityProviderMock.class, BasicBot.class);
		support2.executionEngine.init();
		assertNotNull(support2.botsRegistry.get(actorRef.get()));
	}
	
	@Test(expected = BotNotFoundException.class)
	public void test_create_finish_replay() {
		File dataDir = createTempDir();
		EngineSupportWithLp<LiquidityProviderMock> support1 = new EngineSupportWithLp<>(dataDir, LiquidityProviderMock.class, BasicBot.class);
		support1.executionEngine.init();
		
		RefImpl<BotRef> actorRef = new RefImpl<>();
		support1.executionEngine.execute(CreateBot.createByType(BasicBot.class.getName(), new ParametersBuilder().bool("finish_on_start", true).build(),
				Optional.empty(), newBotRefHandler(actorRef)));
		support1.dispose();
		
		EngineSupportWithLp<LiquidityProviderMock> support2 = new EngineSupportWithLp<>(dataDir, LiquidityProviderMock.class, BasicBot.class);
		support2.executionEngine.init();
		support2.botsRegistry.get(actorRef.get());
	}
	
	@Test(expected = BotNotFoundException.class)
	public void test_create_abort_replay() {
		File dataDir = createTempDir();
		EngineSupportWithLp<LiquidityProviderMock> support1 = new EngineSupportWithLp<>(dataDir, LiquidityProviderMock.class, BasicBot.class);
		support1.executionEngine.init();
		
		RefImpl<BotRef> actorRef = new RefImpl<>();
		support1.executionEngine.execute(CreateBot.createByType(BasicBot.class.getName(), new ParametersBuilder().bool("finish_on_start", false).build(),
				Optional.empty(), newBotRefHandler(actorRef)));
		support1.executionEngine.execute(new AbortBot(actorRef.get(), result -> {}));
		support1.dispose();
		
		EngineSupportWithLp<LiquidityProviderMock> support2 = new EngineSupportWithLp<>(dataDir, LiquidityProviderMock.class, BasicBot.class);
		support2.executionEngine.init();
		support2.botsRegistry.get(actorRef.get());
	}
	
	@Test
	public void test_view_command_replay() {
		File dataDir = createTempDir();
		EngineSupportWithLp<LiquidityProviderMock> support1 = new EngineSupportWithLp<>(dataDir, LiquidityProviderMock.class, BasicBot.class);
		support1.executionEngine.init();
		
		RefImpl<BotRef> actorRef = new RefImpl<>();
		support1.executionEngine.execute(CreateBot.createByType(BasicBot.class.getName(), new ParametersBuilder().bool("finish_on_start", false).build(),
				Optional.empty(), newBotRefHandler(actorRef)));
		RefImpl<Data> pingResultRef = new RefImpl<>();
		support1.executionEngine.execute(new InvokeExecutionBotCommand(actorRef.get(), "PING", new ParametersBuilder().build(), view -> pingResultRef.set(view.result().get())));
		assertEquals("pong", pingResultRef.get().string("result"));
		support1.executionEngine.execute(new InvokeExecutionBotCommand(actorRef.get(), "CHANGE_KEY1", new ParametersBuilder().string("key1", "value1").build(), view -> {}));
		assertEquals("value1", support1.botsRegistry.get(actorRef.get()).view().state().string("key1"));
		BasicBotState botState = support1.botsRegistry.get(actorRef.get()).view().state(BasicBotState.class);
		assertEquals("value1", botState.key1());
		BasicBotState botState2 = (BasicBotState)support1.botsRegistry.get(actorRef.get()).view().state(BasicBotState.class.getSimpleName());
		assertEquals("value1", botState2.key1());
		support1.dispose();
		
		EngineSupportWithLp<LiquidityProviderMock> support2 = new EngineSupportWithLp<>(dataDir, LiquidityProviderMock.class, BasicBot.class);
		support2.executionEngine.init();
		assertEquals("value1", support2.botsRegistry.get(actorRef.get()).view().state().string("key1"));
	}
	
	@Test
	public void test_create_by_feature() {
		File dataDir = createTempDir();
		EngineSupportWithLp<LiquidityProviderMock> support1 = new EngineSupportWithLp<>(dataDir, LiquidityProviderMock.class, BasicBot.class);
		support1.executionEngine.init();
		
		RefImpl<BotRef> actorRef = new RefImpl<>();
		support1.executionEngine.execute(CreateBot.createByFeature("TEST_BOT", new ParametersBuilder().bool("finish_on_start", false).build(),
				Optional.empty(), newBotRefHandler(actorRef)));
		assertNotNull(support1.botsRegistry.get(actorRef.get()));
	}
	
	@Test
	public void test_no_replay_demanding_event_processor() {
		File dataDir = createTempDir();
		EngineSupportWithLp<LiquidityProviderMock> support1 = new EngineSupportWithLp<>(dataDir, LiquidityProviderMock.class, QuotesProcessingBot.class);
		support1.executionEngine.init();
		
		RefImpl<BotView> botView = new RefImpl<>();
		support1.executionEngine.execute(CreateBot.createByType(QuotesProcessingBot.class.getName(), new ParametersBuilder().build(),
				Optional.empty(), newBotViewHandler(botView)));
		assertNotNull(support1.botsRegistry.get(botView.get().ref()));
		assertEquals(0, botView.get().state().integer("counter", 0));
		support1.lp.publishQuote("S1/B", new BigDecimal("1190"), new BigDecimal("1200"));
		support1.lp.publishQuote("S1/B", new BigDecimal("1190"), new BigDecimal("1200"));
		assertEquals(2, botView.get().state().integer("counter", 0));
		support1.dispose();
		
		EngineSupportWithLp<LiquidityProviderMock> support2 = new EngineSupportWithLp<>(dataDir, LiquidityProviderMock.class, QuotesProcessingBot.class);
		support2.executionEngine.init();
		BotView botView2 = support2.botsRegistry.get(botView.get().ref()).view();
		assertNotNull(botView2);
		assertEquals(0, botView2.state().integer("counter", 0));
	}
	
	// TODO exception in command
	
	// TODO agent lifecycle and domain events

	
}
