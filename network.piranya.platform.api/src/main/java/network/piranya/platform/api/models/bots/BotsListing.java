package network.piranya.platform.api.models.bots;

import java.util.List;

import network.piranya.platform.api.models.metadata.BotTypeInfo;

public interface BotsListing {
	
	List<BotView> list();
	List<BotView> byType(String botTypeId);
	List<BotView> byFeature(String featureId);
	BotView singleton(String botTypeId);
	BotTypeInfo botMetadata(String botTypeId);
	BotView bot(BotRef botRef);
	List<BotTypeInfo> botTypes();
	List<BotTypeInfo> botTypesByFeature(String featureId);
	
}
