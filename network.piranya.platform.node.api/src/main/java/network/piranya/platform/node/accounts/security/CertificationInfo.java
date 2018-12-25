package network.piranya.platform.node.accounts.security;

public enum CertificationInfo {
	
	INDEX_NODE(new SignatureVerificationStructure()),
	ACCOUNTING_WATCHDOG_NODE(new SignatureVerificationStructure()),
	EXECUTION_NODE(new SignatureVerificationStructure());
	
	
	CertificationInfo(SignatureVerificationStructure signatureVerificationStructure) {
	}
	
}
