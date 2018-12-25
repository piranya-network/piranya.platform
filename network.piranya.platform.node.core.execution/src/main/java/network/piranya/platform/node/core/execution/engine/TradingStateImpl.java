package network.piranya.platform.node.core.execution.engine;

import java.util.ArrayList;

import network.piranya.platform.api.models.trading.TradingState;

public class TradingStateImpl implements TradingState {
	
	@Override
	public Orders openOrders() {
		return orders;
	}
	
	@Override
	public Trades openTrades() {
		return trades;
	}
	
	
	public TradingStateImpl(TradingRepository tradingRepository) {
		this.orders = new Orders(new ArrayList<>(tradingRepository.orders()));
		this.trades = new Trades(new ArrayList<>(tradingRepository.trades()));
	}
	
	private final Orders orders;
	private final Trades trades;
	
}
