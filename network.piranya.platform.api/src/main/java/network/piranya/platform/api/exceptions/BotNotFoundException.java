package network.piranya.platform.api.exceptions;

public class BotNotFoundException extends PiranyaException {
	
	public BotNotFoundException(String botId) {
		super(String.format("Bot '%s' was not found", botId));
	}
	
	
	private static final long serialVersionUID = ("urn:" + BotNotFoundException.class.getName()).hashCode();
	
}
