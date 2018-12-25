package network.piranya.platform.node.core.execution.storage;

import static network.piranya.platform.node.utilities.CollectionUtils.*;
import static org.junit.Assert.*;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import network.piranya.platform.api.models.bots.BotSpec;
import network.piranya.platform.api.models.bots.BotView;
import network.piranya.platform.node.api.execution.commands.InvokeCommandBot;
import network.piranya.platform.node.core.execution.testing.support.AbstractExecutionTest;
import network.piranya.platform.node.core.execution.testing.support.EngineSupportWithLp;
import network.piranya.platform.node.core.execution.testing.support.LiquidityProviderMock;
import network.piranya.platform.node.utilities.RefImpl;

public class ExecutionStorageTests extends AbstractExecutionTest {
	
	@Test
	public void test_queue_store_read_reply() {
		File dataDir = createTempDir();
		EngineSupportWithLp<LiquidityProviderMock> support1 = new EngineSupportWithLp<>(dataDir, LiquidityProviderMock.class, QuotesRecordingBot.class);
		support1.executionEngine.init();
		
		BotView botView = find(support1.botsRegistry.botsList(), bot -> bot instanceof QuotesRecordingBot).get().view();
		
		support1.lp.publishQuote("S1/B", new BigDecimal("1190"), new BigDecimal("1200"));
		support1.lp.publishQuote("S1/B", new BigDecimal("1191"), new BigDecimal("1201"));
		support1.lp.publishQuote("S1/B", new BigDecimal("1192"), new BigDecimal("1202"));
		
		QuotesQuery query1 = botView.query(QuotesQuery.class);
		List<Quote> queriedQuotes = query1.queryQuotes(Long.MIN_VALUE, Long.MAX_VALUE);
		assertEquals(3, queriedQuotes.size());
		assertEquals(new BigDecimal("1190"), queriedQuotes.get(0).bid());
		assertEquals(new BigDecimal("1191"), queriedQuotes.get(1).bid());
		assertEquals(new BigDecimal("1192"), queriedQuotes.get(2).bid());
		support1.dispose();
		
		EngineSupportWithLp<LiquidityProviderMock> support2 = new EngineSupportWithLp<>(dataDir, LiquidityProviderMock.class, QuotesRecordingBot.class);
		support2.executionEngine.init();
		
		BotView botView2 = find(support2.botsRegistry.botsList(), bot -> bot instanceof QuotesRecordingBot).get().view();
		
		QuotesQuery query2 = botView2.query(QuotesQuery.class);
		List<Quote> queriedQuotes2 = query2.queryQuotes(Long.MIN_VALUE, Long.MAX_VALUE);
		assertEquals(3, queriedQuotes2.size());
		assertEquals(new BigDecimal("1190"), queriedQuotes2.get(0).bid());
		assertEquals(new BigDecimal("1191"), queriedQuotes2.get(1).bid());
		assertEquals(new BigDecimal("1192"), queriedQuotes2.get(2).bid());
	}
	
	@Test
	public void test_queue_read_stream() {
		File dataDir = createTempDir();
		EngineSupportWithLp<LiquidityProviderMock> support1 = new EngineSupportWithLp<>(dataDir, LiquidityProviderMock.class, QuotesRecordingBot.class);
		support1.executionEngine.init();
		
		BotView botView = find(support1.botsRegistry.botsList(), bot -> bot instanceof QuotesRecordingBot).get().view();
		
		support1.lp.publishQuote("S1/B", new BigDecimal("1190"), new BigDecimal("1200"));
		support1.lp.publishQuote("S2/B", new BigDecimal("1191"), new BigDecimal("1201"));
		support1.lp.publishQuote("S1/B", new BigDecimal("1192"), new BigDecimal("1202"));
		
		QuotesQuery_v2 query1 = botView.query(QuotesQuery_v2.class);
		List<Quote> queriedQuotes = new ArrayList<>();
		query1.queryQuotes(Long.MIN_VALUE, Long.MAX_VALUE, "S1/B>LP0", q -> queriedQuotes.add(q));
		assertEquals(2, queriedQuotes.size());
		assertEquals(new BigDecimal("1190"), queriedQuotes.get(0).bid());
		assertEquals(new BigDecimal("1192"), queriedQuotes.get(1).bid());
		
		Iterator<Object> iterator = query1.iterateQuotes();
		assertEquals(new BigDecimal("1190"), ((Quote)iterator.next()).bid());
		assertEquals(new BigDecimal("1191"), ((Quote)iterator.next()).bid());
		assertEquals(new BigDecimal("1192"), ((Quote)iterator.next()).bid());
		assertFalse(iterator.hasNext());
		
		Iterator<Object> iterator2 = query1.iterateQuotes();
		assertTrue(iterator2.hasNext());
		assertTrue(iterator2.hasNext());
		assertEquals(new BigDecimal("1190"), ((Quote)iterator2.next()).bid());
		assertTrue(iterator2.hasNext());
		assertEquals(new BigDecimal("1191"), ((Quote)iterator2.next()).bid());
		assertEquals(new BigDecimal("1192"), ((Quote)iterator2.next()).bid());
		assertFalse(iterator2.hasNext());
		
		support1.dispose();
	}
	
	@Test
	public void test_kv_store_read_reply() throws Exception {
		File dataDir = createTempDir();
		EngineSupportWithLp<LiquidityProviderMock> support1 = new EngineSupportWithLp<>(dataDir, LiquidityProviderMock.class, QuotesKvRecordingBot.class);
		support1.executionEngine.init();
		
		BotView botView = find(support1.botsRegistry.botsList(), bot -> bot instanceof QuotesKvRecordingBot).get().view();
		
		support1.lp.publishQuote("S1/B", new BigDecimal("1190"), new BigDecimal("1200"));
		Thread.sleep(200);
		support1.lp.publishQuote("S1/B", new BigDecimal("1191"), new BigDecimal("1201"));
		Thread.sleep(200);
		support1.lp.publishQuote("S1/B", new BigDecimal("1192"), new BigDecimal("1202"));
		Thread.sleep(200);
		
		QuotesQuery query1 = botView.query(QuotesQuery.class);
		List<Quote> queriedQuotes = query1.queryQuotes(Long.MIN_VALUE, Long.MAX_VALUE);
		assertEquals(3, queriedQuotes.size());
		assertEquals(new BigDecimal("1190"), queriedQuotes.get(0).bid());
		assertEquals(new BigDecimal("1191"), queriedQuotes.get(1).bid());
		assertEquals(new BigDecimal("1192"), queriedQuotes.get(2).bid());
		support1.dispose();
		
		EngineSupportWithLp<LiquidityProviderMock> support2 = new EngineSupportWithLp<>(dataDir, LiquidityProviderMock.class, QuotesKvRecordingBot.class);
		support2.executionEngine.init();
		
		BotView botView2 = find(support2.botsRegistry.botsList(), bot -> bot instanceof QuotesKvRecordingBot).get().view();
		
		QuotesQuery query2 = botView2.query(QuotesQuery.class);
		List<Quote> queriedQuotes2 = query2.queryQuotes(Long.MIN_VALUE, Long.MAX_VALUE);
		assertEquals(3, queriedQuotes2.size());
		assertEquals(new BigDecimal("1190"), queriedQuotes2.get(0).bid());
		assertEquals(new BigDecimal("1191"), queriedQuotes2.get(1).bid());
		assertEquals(new BigDecimal("1192"), queriedQuotes2.get(2).bid());
	}
	
	@Test
	public void test_process_csv_system_file() throws Exception {
		File dataDir = createTempDir();
		EngineSupportWithLp<LiquidityProviderMock> support1 = new EngineSupportWithLp<>(dataDir, LiquidityProviderMock.class, CsvLoaderBot.class);
		support1.executionEngine.init();
		System.out.println(getClass().getResource("test-data.csv").getFile());
		RefImpl<String[]> resultRef = new RefImpl<>();
		support1.executionEngine.execute(new InvokeCommandBot(BotSpec.byType(CsvLoaderBot.class), "load_csv",
				params().string("path", getClass().getResource("test-data.csv").getFile()).build(), result -> resultRef.set((String[])result.result().get())));
		assertTrue(waitUntil(100, () -> !resultRef.isEmpty()));
		support1.dispose();
		
		assertTrue(resultRef.get().length == 3);
		assertEquals("row1", resultRef.get()[0]);
		assertEquals("row2", resultRef.get()[1]);
		assertEquals("row3", resultRef.get()[2]);
	}
	
}
