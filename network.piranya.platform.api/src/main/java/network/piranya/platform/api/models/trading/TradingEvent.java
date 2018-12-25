package network.piranya.platform.api.models.trading;

import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.api.models.bots.BotRef;

public interface TradingEvent extends ExecutionEvent {
	
	String symbol();
	
	Optional<BotRef> sourceBotRef();
	
}
