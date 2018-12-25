package network.piranya.platform.api.extension_models;

public class ExtensionException extends RuntimeException {
	
	public ExtensionException() {
		super();
	}
	
	public ExtensionException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ExtensionException(String message) {
		super(message);
	}
	
	public ExtensionException(Throwable cause) {
		super(cause);
	}
	
	
	private static final long serialVersionUID = ("urn:" + ExtensionException.class.getName()).hashCode();
	
}
