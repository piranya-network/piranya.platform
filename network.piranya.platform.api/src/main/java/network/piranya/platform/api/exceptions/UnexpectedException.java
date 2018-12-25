package network.piranya.platform.api.exceptions;

public class UnexpectedException extends PiranyaException {
	
	public UnexpectedException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public UnexpectedException(Throwable cause) {
		super("Unexpected message was thrown: " + cause.getMessage(), cause);
	}
	
	public UnexpectedException(String message) {
		super(message);
	}
	
	
	private static final long serialVersionUID = ("urn:" + UnexpectedException.class.getName()).hashCode();
	
}
