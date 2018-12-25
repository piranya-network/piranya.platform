package network.piranya.platform.node.core.execution.bots;

import network.piranya.platform.api.extension_models.execution.bots.ExecutionBot;
import network.piranya.platform.api.extension_models.Parameters;
import network.piranya.platform.api.extension_models.execution.bots.BotMetadata;
import network.piranya.platform.api.extension_models.execution.bots.Command;
import network.piranya.platform.api.models.infrastructure.storage.QueueStoreConfig;
import network.piranya.platform.api.models.infrastructure.storage.QueueStore;
import network.piranya.platform.node.core.execution.infrastructure.serialization.json.OrderBookEntry;

@BotMetadata(displayName = "Quotes Downloader Bot")
public class QuotesDownloaderBot extends ExecutionBot implements QuotesDownloaderBotStatus {
	
	@Command(id = "DOWNLOAD_QUOTES")
	public void downloadQuotes(Parameters params, DetachedContext context) {
		doDownloadQuotes(context);
	}
	
	protected void doDownloadQuotes(DetachedContext context) {
		context.net().rest().processLargeJson(params().string("url"))
		.onReply(jsonReader -> {
			QueueStore queue = queue(context);
			jsonReader.readArray(OrderBookEntry.class, entry -> queue.append(entry));
			
			didRead = true;
			context.dispose();
		})
		.onError(ex -> {
			ex.printStackTrace();
			didRead = true;
			context.dispose();
		});
	}
	
	@Override
	public boolean didRead() {
		return didRead;
	}
	
	private boolean didRead = false;
	
	protected QueueStore queue(DetachedContext context) {
		return context.storage().queue(params().string("queue_id"), new QueueStoreConfig(false, true), OrderBookEntry.class);
	}
	
}
