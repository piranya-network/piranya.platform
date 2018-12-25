package network.piranya.platform.api.exceptions;

public class LpNotExistsException extends PiranyaException {
	
	public LpNotExistsException(String lpId) {
		super(String.format("Liquidity Provider '%s' does not exist", lpId));
	}
	
	
	private static final long serialVersionUID = ("urn:" + LpNotExistsException.class.getName()).hashCode();
	
}
