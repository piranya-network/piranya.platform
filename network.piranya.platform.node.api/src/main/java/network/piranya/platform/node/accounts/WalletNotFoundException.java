package network.piranya.platform.node.accounts;

import network.piranya.platform.api.exceptions.PiranyaException;

public class WalletNotFoundException extends PiranyaException {

	public WalletNotFoundException(long walletId) {
		super(String.format("Wallet [%s] was not found", walletId));
	}
	
	private static final long serialVersionUID = ("urn:" + WalletNotFoundException.class.getName()).hashCode();
	
}
