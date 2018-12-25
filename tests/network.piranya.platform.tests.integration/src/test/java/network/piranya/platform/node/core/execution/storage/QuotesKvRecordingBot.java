package network.piranya.platform.node.core.execution.storage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import network.piranya.platform.api.extension_models.execution.bots.ExecutionBot;
import network.piranya.platform.api.extension_models.execution.bots.BotMetadata;
import network.piranya.platform.api.extension_models.execution.bots.EventProcessor;
import network.piranya.platform.api.models.infrastructure.storage.KeyValueStore;
import network.piranya.platform.api.models.trading.liquidity.QuoteEvent;

@BotMetadata(displayName = "Quotes Kv Recording Bot", singleton = true)
public class QuotesKvRecordingBot extends ExecutionBot implements QuotesQuery, QuotesQuery_v2 {
	
	@Override
	public void onStart(ExecutionContext context) {
		this.keyValueReader = context.storage().useOutOfContext(context.storage().keyValue("quotes"));
	}
	
	@EventProcessor(demandOnReplay = false)
	public void onQuote(QuoteEvent quote, ExecutionContext context) {
		keyValueStore(context).put(String.format("%s:%s", quote.quote().instrument().symbol(), index++), quote.quote().bid().toPlainString());
	}
	
	
	@Override
	public List<Quote> queryQuotes(long startTime, long endTime) {
		List<Quote> result = new ArrayList<>();
		//System.err.println(keyValueReader.get("S1:0"));
		keyValueReader.iterate("", (key, value) -> result.add(new Quote(key.substring(0, key.indexOf(':')), new BigDecimal(value), new BigDecimal(value))));
		return result;
	}
	
	@Override
	public void queryQuotes(long startTime, long endTime, String symbol, Consumer<Quote> consumer) {
		keyValueReader.iterate(symbol + ":", (key, value) -> consumer.accept(new Quote(symbol, new BigDecimal(value), new BigDecimal(value))));
	}
	
	@Override
	public Iterator<Object> iterateQuotes() {
		throw new RuntimeException("Not Implemented");
	}
	
	
	protected KeyValueStore keyValueStore(ExecutionContext context) {
		return context.storage().keyValue("quotes");
	}
	
	private KeyValueStore keyValueReader;
	
	private int index = 0;
	
}
