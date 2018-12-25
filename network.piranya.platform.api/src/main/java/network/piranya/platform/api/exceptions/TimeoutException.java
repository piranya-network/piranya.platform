package network.piranya.platform.api.exceptions;

public class TimeoutException extends Exception {
	
	public TimeoutException() {
		this("Operation timedout");
	}
	
	public TimeoutException(String message) {
		super(message);
	}
	
	private static final long serialVersionUID = ("urn:" + TimeoutException.class.getName()).hashCode();
	
}
