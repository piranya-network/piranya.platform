package network.piranya.platform.node.core.execution.bots;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.api.models.bots.BotView;
import network.piranya.platform.node.api.execution.commands.CreateBot;
import network.piranya.platform.node.api.execution.commands.InvokeExecutionBotCommand;
import network.piranya.platform.node.core.execution.testing.support.AbstractExecutionTest;
import network.piranya.platform.node.core.execution.testing.support.EngineSupportWithLp;
import network.piranya.platform.node.core.execution.testing.support.LiquidityProviderMock;
import network.piranya.platform.node.core.execution.testing.utils.HttpServerUtils;
import network.piranya.platform.node.utilities.RefImpl;

public class DetachedContextTests extends AbstractExecutionTest {
	
	@Test
	public void test_detach_context() throws Exception {
		File dataDir = createTempDir();
		try (HttpServerUtils http = new HttpServerUtils()) {
			String fullUrl = http.serveJsonFile("/json", new String(Files.readAllBytes(Paths.get(getClass().getResource("BITFINEX_SPOT_BTC_USD-ob-0420.txt").toURI()))));
			
			EngineSupportWithLp<LiquidityProviderMock> support1 = new EngineSupportWithLp<>(dataDir, LiquidityProviderMock.class, QuotesDownloaderBot.class, QuotesReaderBot.class);
			support1.executionEngine.init();
			
			String queueId = "order-bot-queue";
			
			RefImpl<BotView> readerBotView = new RefImpl<>();
			support1.executionEngine.execute(CreateBot.createByType(QuotesReaderBot.class.getName(),
					params().string("queue_id", queueId).build(),
					Optional.empty(), newBotViewHandler(readerBotView)));
			
			RefImpl<BotView> loaderBotView = new RefImpl<>();
			support1.executionEngine.execute(CreateBot.createByType(QuotesDownloaderBot.class.getName(),
					params().bool("load_on_start", true).string("url", fullUrl).string("queue_id", queueId).build(),
					Optional.empty(), newBotViewHandler(loaderBotView)));
			support1.executionEngine.execute(new InvokeExecutionBotCommand(loaderBotView.get().ref(), "DOWNLOAD_QUOTES", params().build(), view -> {}));
			
			assertTrue(waitUntil(1000, () -> loaderBotView.get().query(QuotesDownloaderBotStatus.class).didRead()));
			
			AtomicInteger counter = new AtomicInteger(0);
			readerBotView.get().query(OrderBookReader.class).readOrderBookEntries(entry -> counter.incrementAndGet());
			assertEquals(100, counter.get());
			support1.dispose();
			
			EngineSupportWithLp<LiquidityProviderMock> support2 = new EngineSupportWithLp<>(dataDir, LiquidityProviderMock.class, QuotesDownloaderBot.class, QuotesReaderBot.class);
			support2.executionEngine.init();
			BotView readerBotView2 = support2.botsRegistry.get(readerBotView.get().ref()).view();
			AtomicInteger counter2 = new AtomicInteger(0);
			readerBotView2.query(OrderBookReader.class).readOrderBookEntries(entry -> counter2.incrementAndGet());
			assertEquals(100, counter2.get());
			support2.dispose();
		}
		/*
		RefImpl<BotRef> actorRef = new RefImpl<>();
		support1.executionEngine.execute(CreateBot.createByType(BasicBot.class.getName(), new ParametersBuilder().bool("finish_on_start", false).build(),
				Optional.empty(), newBotRefHandler(actorRef)));
		assertNotNull(support1.botsRegistry.get(actorRef.get()));
		support1.dispose();
		
		EngineSupportWithLp<LiquidityProviderMock> support2 = new EngineSupportWithLp<>(dataDir, LiquidityProviderMock.class, BasicBot.class);
		support2.executionEngine.init();
		assertNotNull(support2.botsRegistry.get(actorRef.get()));
		*/
	}
	
}
