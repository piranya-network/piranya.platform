package network.piranya.platform.node.core.execution.bots;

import network.piranya.platform.api.extension_models.execution.bots.ExecutionBot;
import network.piranya.platform.api.extension_models.execution.bots.BotMetadata;
import network.piranya.platform.api.extension_models.execution.bots.EventProcessor;
import network.piranya.platform.api.models.trading.liquidity.QuoteEvent;

@BotMetadata(displayName = "Quotes Processing Bot")
public class QuotesProcessingBot extends ExecutionBot {
	
	@EventProcessor(demandOnReplay = false)
	public void onQuote(QuoteEvent quote) {
		updateState(utils().params().integer("counter", view().state().integer("counter", 0) + 1).build());
	}
	
}
