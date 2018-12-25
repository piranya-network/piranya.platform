package network.piranya.platform.node.core.execution.bots;

import java.util.function.Consumer;

import network.piranya.platform.node.core.execution.infrastructure.serialization.json.OrderBookEntry;

public interface OrderBookReader {
	
	void readOrderBookEntries(Consumer<OrderBookEntry> consumer);
	
}
