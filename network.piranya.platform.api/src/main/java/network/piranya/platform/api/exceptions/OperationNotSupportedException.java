package network.piranya.platform.api.exceptions;

public class OperationNotSupportedException extends PiranyaException {
	
	public OperationNotSupportedException(String message) {
		super(message);
	}
	
	
	private static final long serialVersionUID = ("urn:" + OperationNotSupportedException.class.getName()).hashCode();
	
}
