package network.piranya.platform.node.core.execution.sandbox;

import network.piranya.platform.api.extension_models.execution.bots.ExecutionBot;
import network.piranya.platform.api.extension_models.execution.bots.BotEvent;
import network.piranya.platform.api.extension_models.execution.bots.EventProcessor;

public class StatsGatheringBot extends ExecutionBot {
	
	@EventProcessor
	public void onEvent(BotEvent event, ExecutionContext context) {
		if (event.botTypeId().contains("MarketOrderBot") && event.eventTypeId().equals(BotEvent.FINISHED_EVENT_TYPE_ID)) {
			updateState(utils().params().integer("fillsCount", ++fillsCount).build());
		}
	}
	
	private int fillsCount = 0;
	
}
