package network.piranya.platform.api.exceptions;

public class InvalidOperationException extends PiranyaException {
	
	public InvalidOperationException(String message) {
		super(message);
	}
	
	
	private static final long serialVersionUID = ("urn:" + InvalidOperationException.class.getName()).hashCode();
	
}
