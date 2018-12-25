package network.piranya.platform.tests.commons.node;

import java.util.Arrays;

import network.piranya.platform.api.accounting.AccountRef;
import network.piranya.platform.api.accounting.NetworkCredentials;
import network.piranya.platform.api.accounting.NodeId;
import network.piranya.platform.api.crypto.CryptoKey;
import network.piranya.platform.api.crypto.KeyPair;
import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.node.accounts.Account;
import network.piranya.platform.node.accounts.activity.AccountActivity;
import network.piranya.platform.node.accounts.security.AccountSecurity;
import network.piranya.platform.node.accounts.security.CertificationInfo;
import network.piranya.platform.node.api.networking.nodes.NodeContacts;
import network.piranya.platform.node.api.networking.nodes.Zone;
import network.piranya.platform.node.utilities.CryptoUtils;

public class TestAccount {
	
	public NetworkCredentials credentials() {
		return credentials;
	}
	
	public Account account() {
		return account;
	}
	
	public String nodeId() {
		return nodeId;
	}
	
	public String host() {
		return host;
	}
	
	public int port() {
		return port;
	}
	
	
	public TestAccount(int port, CertificationInfo... certifications) {
		this.port = port;
		this.signatureKeyPair = CryptoUtils.instance().generateSignatureKeyPair();
		this.encryptionKeyPair = CryptoUtils.instance().generateSealedBoxKeyPair();
		this.accountId = new AccountRef(CryptoUtils.instance().digest(this.signatureKeyPair.publicKey()));
		this.nodeId = host + ":" + port;
		
		this.credentials = new NetworkCredentials(accountId, new CryptoKey(signatureKeyPair.privateKey()));
		this.account = Account.create(accountId, Optional.empty(),
				new AccountSecurity(new CryptoKey(signatureKeyPair.publicKey()), new CryptoKey(encryptionKeyPair.publicKey()), Arrays.asList(certifications)),
				new AccountActivity(Arrays.asList(new AccountActivity.AccessPoint[] {
						new AccountActivity.AccessPoint(new NodeContacts(new NodeId(nodeId, 0), host, port, accountId, Zone.ANY), 0L) })));
	}
	
	private final KeyPair signatureKeyPair;
	private final KeyPair encryptionKeyPair;
	private final AccountRef accountId;
	private final int port;
	private final String nodeId;
	private final String host = "localhost";
	private final Account account;
	private NetworkCredentials credentials;
	
}
