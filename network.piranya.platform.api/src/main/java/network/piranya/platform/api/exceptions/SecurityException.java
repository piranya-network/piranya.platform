package network.piranya.platform.api.exceptions;

public class SecurityException extends Exception {
	
	public SecurityException(String message) {
		super(message);
	}
	
	private static final long serialVersionUID = ("urn:" + SecurityException.class.getName()).hashCode();
	
}
