package network.piranya.platform.api.exceptions;

public class PiranyaException extends RuntimeException {
	
	public PiranyaException() {
		super();
	}
	
	public PiranyaException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public PiranyaException(String message) {
		super(message);
	}
	
	public PiranyaException(Throwable cause) {
		super(cause);
	}
	
	
	private static final long serialVersionUID = ("urn:" + PiranyaException.class.getName()).hashCode();
	
}
