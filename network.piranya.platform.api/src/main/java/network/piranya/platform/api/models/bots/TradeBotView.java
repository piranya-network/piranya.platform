package network.piranya.platform.api.models.bots;

import java.util.Set;

public interface TradeBotView extends BotView {
	
	Set<Long> subTrades();
	
}
