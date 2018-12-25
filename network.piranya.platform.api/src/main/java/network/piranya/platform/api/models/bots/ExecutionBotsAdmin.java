package network.piranya.platform.api.models.bots;

import java.util.function.Consumer;

import network.piranya.platform.api.extension_models.Data;
import network.piranya.platform.api.extension_models.Parameters;
import network.piranya.platform.api.extension_models.execution.bots.BotEvent;
import network.piranya.platform.api.lang.Result;

public interface ExecutionBotsAdmin extends BotsListing {
	
	void startBot(BotSpec spec, Parameters params, Consumer<Result<BotView>> resultHandler, Consumer<BotEvent> eventsSubscriber);
	void startBot(BotSpec spec, Parameters params, Consumer<Result<BotView>> resultHandler);
	void invokeBot(BotRef botRef, String commandId, Parameters params, Consumer<Result<Data>> resultHandler);
	void abortBot(BotRef botRef, Consumer<Result<BotView>> resultHandler);
	
}
