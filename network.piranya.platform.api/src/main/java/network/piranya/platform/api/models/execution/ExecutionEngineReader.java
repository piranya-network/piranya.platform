package network.piranya.platform.api.models.execution;

import java.util.function.Consumer;

import network.piranya.platform.api.exceptions.PricesNotAvailableException;
import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.api.models.bots.BotsListing;
import network.piranya.platform.api.models.trading.ExecutionEvent;
import network.piranya.platform.api.models.trading.Instrument;
import network.piranya.platform.api.models.trading.Quote;
import network.piranya.platform.api.models.trading.TradingState;

public interface ExecutionEngineReader {
	
	BotsListing bots();
	
	TradingState tradingState();
	Quote quote(Instrument instrument) throws PricesNotAvailableException;
	Optional<Quote> getQuote(Instrument instrument);
	
	void subscribe(Consumer<ExecutionEvent> subscriber);
	<EventType extends ExecutionEvent> void subscribe(Class<EventType> eventType, Consumer<EventType> subscriber);
	<EventType extends ExecutionEvent> void unsubscribe(Consumer<EventType> subscriber);
	
}
