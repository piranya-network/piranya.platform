package network.piranya.platform.node.utilities;

import network.piranya.platform.api.extension_models.execution.bots.ExecutionBot;
import network.piranya.platform.api.extension_models.execution.bots.BotMetadata;

public class BotUtils {
	
	public static <BotType extends ExecutionBot> boolean isSingleton(Class<BotType> botType) {
		boolean result = false;
		if (botType.isAnnotationPresent(BotMetadata.class)) {
			result = botType.getDeclaredAnnotation(BotMetadata.class).singleton();
		}
		return result;
	}
	
	
	private BotUtils() { }
	
}
