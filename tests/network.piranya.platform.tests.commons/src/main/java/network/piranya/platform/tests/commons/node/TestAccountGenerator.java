package network.piranya.platform.tests.commons.node;

import network.piranya.platform.node.accounts.security.CertificationInfo;

public class TestAccountGenerator {
	
	public TestAccount generateAcount(int port) {
		return new TestAccount(port);
	}

	public TestAccount generateIndexAcount(int port) {
		return new TestAccount(port, CertificationInfo.INDEX_NODE);
	}
	
}
