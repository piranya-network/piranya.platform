package network.piranya.platform.api.exceptions;

public class EncodingException extends PiranyaException {
	
	public EncodingException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public EncodingException(Throwable cause) {
		super("Encoding error: " + cause.getMessage(), cause);
	}
	
	public EncodingException(String message) {
		super(message);
	}
	
	
	private static final long serialVersionUID = ("urn:" + EncodingException.class.getName()).hashCode();
	
}
