package network.piranya.platform.node.api.networking.nodes;

import network.piranya.platform.api.accounting.AccountRef;

public abstract class Message {
	
	// Signature[] verifiers;
	
	private AccountRef sourceAccountId;
	public AccountRef sourceAccountId() {
		return sourceAccountId;
	}
	public void setSourceAccountId(AccountRef sourceAccountId) {
		this.sourceAccountId = sourceAccountId;
	}
	
	/*
	public NetworkAddress senderAddress() {
		throw new RuntimeException("NotImplemented");
	}
	*/
	
}
