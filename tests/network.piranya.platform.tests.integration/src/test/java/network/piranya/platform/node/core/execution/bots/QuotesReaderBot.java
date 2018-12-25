package network.piranya.platform.node.core.execution.bots;

import network.piranya.platform.api.extension_models.execution.bots.ExecutionBot;
import network.piranya.platform.api.models.infrastructure.storage.QueueStore;

import java.util.function.Consumer;

import network.piranya.platform.api.extension_models.execution.bots.BotMetadata;
import network.piranya.platform.node.core.execution.infrastructure.serialization.json.OrderBookEntry;

@BotMetadata(displayName = "Quotes Reader Bot")
public class QuotesReaderBot extends ExecutionBot implements OrderBookReader {
	
	@Override
	public void onStart(ExecutionContext context) {
		this.detachedContext = context.acquireDetachedContext();
	}
	
	@Override
	public void readOrderBookEntries(Consumer<OrderBookEntry> consumer) {
		queue().read(OrderBookEntry.class, consumer);
	}
	
	protected QueueStore queue() {
		return detachedContext.storage().globalQueue(params().string("queue_id"), OrderBookEntry.class);
	}
	
	private DetachedContext detachedContext;
	
}
