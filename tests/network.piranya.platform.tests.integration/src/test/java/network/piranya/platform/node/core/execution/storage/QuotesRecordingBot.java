package network.piranya.platform.node.core.execution.storage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import network.piranya.platform.api.extension_models.execution.bots.ExecutionBot;
import network.piranya.platform.api.extension_models.execution.bots.BotMetadata;
import network.piranya.platform.api.extension_models.execution.bots.EventProcessor;
import network.piranya.platform.api.models.infrastructure.storage.QueueStoreConfig;
import network.piranya.platform.api.models.infrastructure.storage.QueueStore;
import network.piranya.platform.api.models.infrastructure.storage.QueueStoreReader;
import network.piranya.platform.api.models.trading.liquidity.QuoteEvent;

@BotMetadata(displayName = "Quotes Recording Bot", singleton = true)
public class QuotesRecordingBot extends ExecutionBot implements QuotesQuery, QuotesQuery_v2 {
	
	@Override
	public void onStart(ExecutionContext context) {
		this.queueReader = context.storage().useOutOfContext(context.storage().queue("quotes", new QueueStoreConfig(true, false), Quote.class));
	}
	
	@EventProcessor(demandOnReplay = false)
	public void onQuote(QuoteEvent quote, ExecutionContext context) {
		queue(context).append(new Quote(quote.quote().instrument().symbol(), quote.quote().bid(), quote.quote().ask()));
	}
	
	
	@Override
	public List<Quote> queryQuotes(long startTime, long endTime) {
		List<Quote> result = new ArrayList<>();
		queueReader.read(Quote.class, q -> result.add(q));
		return result;
	}
	
	@Override
	public void queryQuotes(long startTime, long endTime, String symbol, Consumer<Quote> consumer) {
		queueReader.read(startTime, endTime, event -> {
			if (event instanceof Quote) {
				Quote q = (Quote)event;
				if (q.symbol().equals(symbol)) {
					consumer.accept(q);
				}
			}
		});
	}
	
	@Override
	public Iterator<Object> iterateQuotes() {
		return queueReader.read();
	}
	
	
	protected QueueStore queue(ExecutionContext context) {
		return context.storage().queue("quotes");
	}
	
	private QueueStoreReader queueReader;
	
}
