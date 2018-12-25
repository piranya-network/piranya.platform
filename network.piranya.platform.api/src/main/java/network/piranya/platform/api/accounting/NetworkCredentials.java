package network.piranya.platform.api.accounting;

import network.piranya.platform.api.crypto.CryptoKey;

public class NetworkCredentials {
	
	private final AccountRef accountRef;
	public AccountRef accountRef() {
		return accountRef;
	}
	
	private final CryptoKey privateKey;
	public CryptoKey privateKey() {
		return privateKey;
	}
	
	public NetworkCredentials(AccountRef accountRef, CryptoKey privateKey) {
		this.accountRef = accountRef;
		this.privateKey = privateKey;
	}
	
}
