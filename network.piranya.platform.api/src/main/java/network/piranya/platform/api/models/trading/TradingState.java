package network.piranya.platform.api.models.trading;

import java.util.List;

import network.piranya.platform.api.exceptions.OrderNotFoundException;
import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.api.models.bots.BotRef;
import network.piranya.platform.api.models.trading.filling.OpenTrade;
import network.piranya.platform.api.models.trading.ordering.OrderRef;
import network.piranya.platform.api.models.trading.ordering.PendingOrder;
import network.piranya.platform.api.utilities.Utilities;

public interface TradingState {
	
	Orders openOrders();
	
	Trades openTrades();
	
	
	public static class Orders {
		
		public List<PendingOrder> orders() { return orders; }
		
		public PendingOrder order(OrderRef orderRef) throws OrderNotFoundException {
			return utils.col.find(orders, o -> o.orderRef().equals(orderRef)).orElseThrow(() -> new OrderNotFoundException(orderRef));
		}
		public Optional<PendingOrder> get(OrderRef orderRef) { return utils.col.find(orders, o -> o.orderRef().equals(orderRef)); }
		
		public List<PendingOrder> byBotRef(BotRef botRef) { return utils.col.filter(orders, o -> o.creatorBotRef().equals(botRef)); }
		public List<PendingOrder> byBotId(String botId) { return utils.col.filter(orders, o -> o.creatorBotRef().botId().equals(botId)); }
		
		
		public Orders(List<PendingOrder> orders) {
			this.orders = orders;
		}
		
		private final List<PendingOrder> orders;
		
		private static final Utilities utils = new Utilities();
	}
	
	public static class Trades {
		
		public List<OpenTrade> trades() { return trades; }
		
		public OpenTrade trade(String tradeId) { return utils.col.find(trades, t -> t.tradeId().equals(tradeId)).orElseThrow(() -> new RuntimeException()); }
		public Optional<OpenTrade> get(String tradeId) { return utils.col.find(trades, t -> t.tradeId().equals(tradeId)); }
		
		public List<OpenTrade> byBotRef(BotRef botRef) { return utils.col.filter(trades, t -> t.fills().get(0).order().creatorBotRef().equals(botRef)); }
		public List<OpenTrade> byBotId(String botId) { return utils.col.filter(trades, t -> t.fills().get(0).order().creatorBotRef().botId().equals(botId)); }
		
		
		public Trades(List<OpenTrade> trades) {
			this.trades = trades;
		}
		
		private final List<OpenTrade> trades;
		
		private static final Utilities utils = new Utilities();
	}
	
}
