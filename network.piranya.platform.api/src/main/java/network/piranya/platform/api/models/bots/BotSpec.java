package network.piranya.platform.api.models.bots;

import network.piranya.platform.api.lang.Optional;

import network.piranya.platform.api.extension_models.execution.bots.Bot;

public class BotSpec {
	
	private final Optional<String> botTypeId;
	public Optional<String> botTypeId() { return botTypeId; }
	
	private final Optional<String> featureId;
	public Optional<String> featureId() { return featureId; }
	
	private BotSpec(Optional<String> botTypeId, Optional<String> featureId) {
		this.botTypeId = botTypeId;
		this.featureId = featureId;
	}
	
	
	public static BotSpec byType(String botTypeId) {
		return new BotSpec(Optional.of(botTypeId), Optional.empty());
	}
	
	public static <BotType extends Bot> BotSpec byType(Class<BotType> botType) {
		return byType(botType.getName());
	}
	
	public static BotSpec byFeature(String featureId) {
		return new BotSpec(Optional.empty(), Optional.of(featureId));
	}
	
}
