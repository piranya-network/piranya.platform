package network.piranya.platform.api.models.bots;

import network.piranya.platform.api.extension_models.Parameters;
import network.piranya.platform.api.lang.ResultHandler;

public interface CommandBotsAdmin {
	
	void invokeCommandBot(BotSpec spec, String commandId, Parameters params, ResultHandler<Object> resultHandler);
	
}
