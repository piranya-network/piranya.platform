package network.piranya.platform.node.accounts.security;

import java.util.List;

import network.piranya.platform.api.crypto.CryptoKey;

public class AccountSecurity {
	
	private final CryptoKey publicSigningKey;
	public CryptoKey publicSigningKey() {
		return publicSigningKey;
	}
	
	private final CryptoKey publicEncryptionKey;
	public CryptoKey publicEncryptionKey() {
		return publicEncryptionKey;
	}
	
	private final List<CertificationInfo> certifications;
	public List<CertificationInfo> certifications() {
		return certifications;
	}
	
	public AccountSecurity(CryptoKey publicSigningKey, CryptoKey publicEncryptionKey, List<CertificationInfo> certifications) {
		this.publicSigningKey = publicSigningKey;
		this.publicEncryptionKey = publicEncryptionKey;
		this.certifications = certifications;
	}
	
}
