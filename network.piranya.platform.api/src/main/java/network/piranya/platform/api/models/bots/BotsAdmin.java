package network.piranya.platform.api.models.bots;

import java.util.List;

import network.piranya.platform.api.models.metadata.BotTypeInfo;

public interface BotsAdmin {
	
	ExecutionBotsAdmin execution();
	
	CommandBotsAdmin commands();
	
	BotTypeInfo botMetadata(String botTypeId);
	List<BotTypeInfo> botTypes();
	
}
