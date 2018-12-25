package network.piranya.platform.api.exceptions;

public class FeatureImplementationNotFoundException extends PiranyaException {
	
	public FeatureImplementationNotFoundException(String featureId) {
		super(String.format("An implementation/bot for feature '%s' was not found", featureId));
	}
	
	
	private static final long serialVersionUID = ("urn:" + FeatureImplementationNotFoundException.class.getName()).hashCode();
	
}
