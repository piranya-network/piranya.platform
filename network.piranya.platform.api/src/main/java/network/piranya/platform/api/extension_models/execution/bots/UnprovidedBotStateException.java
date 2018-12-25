package network.piranya.platform.api.extension_models.execution.bots;

import network.piranya.platform.api.exceptions.PiranyaException;

public class UnprovidedBotStateException extends PiranyaException {
	
	public UnprovidedBotStateException(Class<?> botType, String stateType) {
		super(String.format("Bot '%s' does not provide state of type '%s'", botType.getName(), stateType));
	}
	
	
	private static final long serialVersionUID = ("urn:" + UnprovidedBotStateException.class.getName()).hashCode();
	
}
