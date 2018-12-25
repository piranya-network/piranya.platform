package network.piranya.platform.api.models.bots;

import java.util.function.Consumer;

import network.piranya.platform.api.extension_models.Data;
import network.piranya.platform.api.extension_models.Parameters;
import network.piranya.platform.api.extension_models.execution.bots.BotEvent;
import network.piranya.platform.api.extension_models.execution.bots.UnprovidedBotStateException;

public interface BotView {
	
	BotRef ref();
	
	String botTypeId();
	
	String label();
	String description();
	Parameters params();
	
	Data state();
	<T> T state(String stateType) throws UnprovidedBotStateException;
	<T> T state(Class<T> stateClass) throws UnprovidedBotStateException;
	
	void subscribe(Consumer<BotEvent> listener);
	void unsubscribe(Consumer<BotEvent> listener);
	
	<QueryInterface> QueryInterface query(Class<QueryInterface> queryInterface);
	
}
