package network.piranya.platform.node.core.execution.commands;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.junit.Test;

import network.piranya.platform.api.models.bots.BotSpec;
import network.piranya.platform.node.api.execution.commands.InvokeCommandBot;
import network.piranya.platform.node.core.execution.bots.BasicBot;
import network.piranya.platform.node.core.execution.testing.support.AbstractExecutionTest;
import network.piranya.platform.node.core.execution.testing.support.EngineSupportWithLp;
import network.piranya.platform.node.core.execution.testing.support.LiquidityProviderMock;
import network.piranya.platform.node.utilities.RefImpl;

public class CommandBotTests extends AbstractExecutionTest {
	
	@Test
	public void test_command() {
		File dataDir = createTempDir();
		EngineSupportWithLp<LiquidityProviderMock> support1 = new EngineSupportWithLp<>(dataDir, LiquidityProviderMock.class, BasicBot.class, BasicCommandBot.class);
		support1.executionEngine.init();
		
		RefImpl<Object> resultRef = new RefImpl<>();
		assertEquals(0, support1.botsRegistry.botsList().size());
		support1.executionEngine.execute(new InvokeCommandBot(BotSpec.byType(BasicCommandBot.class), "start_bot", params().build(), result -> resultRef.set(result.result().get())));
		assertTrue(waitUntil(100, () -> !resultRef.isEmpty()));
		assertEquals(1, support1.botsRegistry.botsList().size());
		support1.dispose();
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void test_sync_query() {
		File dataDir = createTempDir();
		EngineSupportWithLp<LiquidityProviderMock> support1 = new EngineSupportWithLp<>(dataDir, LiquidityProviderMock.class, BasicBot.class, BasicCommandBot.class);
		support1.executionEngine.init();
		
		RefImpl<Object> resultRef = new RefImpl<>();
		support1.executionEngine.execute(new InvokeCommandBot(BotSpec.byType(BasicCommandBot.class), "query_lps", params().build(), result -> resultRef.set(result.result().get())));
		assertTrue(waitUntil(100, () -> !resultRef.isEmpty()));
		assertEquals("LP0", ((List<LpInfo>)resultRef.get()).get(0).name());
	}
	
	@Test
	public void test_sync_query_with_params() {
		File dataDir = createTempDir();
		EngineSupportWithLp<LiquidityProviderMock> support1 = new EngineSupportWithLp<>(dataDir, LiquidityProviderMock.class, BasicBot.class, BasicCommandBot.class);
		support1.executionEngine.init();
		
		RefImpl<Object> resultRef = new RefImpl<>();
		support1.executionEngine.execute(new InvokeCommandBot(BotSpec.byType(BasicCommandBot.class), "ping", params().string("message", "test").build(),
				result -> resultRef.set(result.result().get())));
		assertTrue(waitUntil(100, () -> !resultRef.isEmpty()));
		assertEquals("pong test", (String)resultRef.get());
	}
	
	@Test
	public void test_async_query_without_params() {
		File dataDir = createTempDir();
		EngineSupportWithLp<LiquidityProviderMock> support1 = new EngineSupportWithLp<>(dataDir, LiquidityProviderMock.class, BasicBot.class, BasicCommandBot.class);
		support1.executionEngine.init();
		
		RefImpl<Object> resultRef = new RefImpl<>();
		support1.executionEngine.execute(new InvokeCommandBot(BotSpec.byType(BasicCommandBot.class), "ping_async", params().build(),
				result -> resultRef.set(result.result().get())));
		assertTrue(waitUntil(100, () -> !resultRef.isEmpty()));
		assertEquals("pong", (String)resultRef.get());
	}
	
	@Test
	public void test_exceptional_command() {
		File dataDir = createTempDir();
		EngineSupportWithLp<LiquidityProviderMock> support1 = new EngineSupportWithLp<>(dataDir, LiquidityProviderMock.class, BasicBot.class, BasicCommandBot.class);
		support1.executionEngine.init();
		
		RefImpl<String> errorRef = new RefImpl<>();
		support1.executionEngine.execute(new InvokeCommandBot(BotSpec.byType(BasicCommandBot.class), "exceptional_command", params().build(),
				result -> errorRef.set(result.error().get().getMessage())));
		assertTrue(waitUntil(100, () -> !errorRef.isEmpty()));
		assertEquals("error", errorRef.get());
	}
	
}
