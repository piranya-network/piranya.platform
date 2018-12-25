package network.piranya.platform.node.core.execution.sandbox;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.api.models.bots.BotRef;
import network.piranya.platform.node.api.execution.commands.InvokeExecutionBotCommand;
import network.piranya.platform.node.api.execution.commands.CreateBot;
import network.piranya.platform.node.core.execution.testing.support.AbstractExecutionTest;
import network.piranya.platform.node.core.execution.testing.support.EngineSupportWithLp;
import network.piranya.platform.node.core.execution.testing.utils.FileUtils;
import network.piranya.platform.node.core.execution.trading.MarketOrderBot;
import network.piranya.platform.node.utilities.RefImpl;

public class SandboxTests extends AbstractExecutionTest {
	
	@Test
	public void testSandbox() throws Exception {
		File dataDir = FileUtils.createTempDir();
		EngineSupportWithLp<SandboxLiquidityProvider> support1 = new EngineSupportWithLp<>(dataDir, SandboxLiquidityProvider.class, SandboxBot.class, MarketOrderBot.class);
		support1.executionEngine.init();
		
		RefImpl<BotRef> botRef = new RefImpl<>();
		support1.executionEngine.execute(CreateBot.createByType(SandboxBot.class.getName(), params().build(), Optional.empty(), newBotRefHandler(botRef)));
		assertNotNull(support1.botsRegistry.get(botRef.get()));
		
		assertEquals(0, support1.botsRegistry.get(botRef.get()).view().state().integer("fillsCount"));
		support1.executionEngine.execute(new InvokeExecutionBotCommand(botRef.get(), "RUN_SANDBOX", params().build(), view -> {}));
		assertTrue(waitUntil(500, () -> support1.botsRegistry.get(botRef.get()).view().state().integer("fillsCount", 0) == 1));
		assertEquals(1, support1.botsRegistry.get(botRef.get()).view().state().integer("acceptedOrdersCount", 0));
		assertEquals(1, support1.botsRegistry.get(botRef.get()).view().state().integer("fillsCount"));
	}
	
	// abort too long
	
	// restart
	
}
