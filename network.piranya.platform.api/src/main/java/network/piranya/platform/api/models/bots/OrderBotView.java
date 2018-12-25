package network.piranya.platform.api.models.bots;

import java.util.Set;

public interface OrderBotView extends BotView {
	
	Set<Long> subOrders();
	
}
