package network.piranya.platform.api.exceptions;

public class InvalidParameterException extends PiranyaException {
	
	private final String parameterId;
	public String getParameterId() {
		return parameterId;
	}
	
	public InvalidParameterException(String message, String parameterId) {
		this(message, parameterId, true);
	}
	
	public InvalidParameterException(String message, String parameterId, boolean isFormat) {
		super(isFormat ? String.format(message, parameterId) : message);
		this.parameterId = parameterId;
	}
	
	
	private static final long serialVersionUID = ("urn:" + InvalidParameterException.class.getName()).hashCode();
	
	public static final String REQUIRED_MESSAGE = "Parameter '%s' is required";
	public static final String INVALID_VALUE_MESSAGE = "The value of %s is invalid";
	
}
