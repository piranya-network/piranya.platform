package network.piranya.platform.node.core.local_infrastructure.net;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import network.piranya.infrastructure.pressing_udp.Message;
import network.piranya.platform.api.accounting.AccountRef;
import network.piranya.platform.api.accounting.NetworkCredentials;
import network.piranya.platform.api.crypto.CryptoKey;
import network.piranya.platform.api.crypto.KeyPair;
import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.node.accounts.Account;
import network.piranya.platform.node.accounts.security.AccountSecurity;
import network.piranya.platform.node.api.local_infrastructure.storage.specific.AccountsLocalDb;
import network.piranya.platform.node.core.local_infrastructure.security.MessageSecurityManager;
import network.piranya.platform.node.core.networks.index.client.messages.Ping;
import network.piranya.platform.node.utilities.CryptoUtils;

public class MessagesEncoderTest {
	
	@Test
	public void testEncodeAndDecode() {
		Message encodedMessage = messagesEncoder.encode(new Ping(100));
		
		Ping ping = (Ping)messagesEncoder.decode(encodedMessage);
		assertEquals(100, ping.instructions());
	}
	
	
	@Before
	public void setup() {
		signatureKeyPair = CryptoUtils.instance().generateSignatureKeyPair();
		
		accountRef = new AccountRef(CryptoUtils.instance().digest(signatureKeyPair.publicKey()));
		account = Account.create(accountRef, Optional.empty(),
				new AccountSecurity(new CryptoKey(signatureKeyPair.publicKey()), null, null));
		
		AccountsLocalDb accountsLocalDb = Mockito.mock(AccountsLocalDb.class);
		Mockito.when(accountsLocalDb.findAccount(accountRef)).thenReturn(Optional.of(account));
		
		this.messagesEncoder = new MessagesEncoder(new MessageSecurityManager(new NetworkCredentials(accountRef, new CryptoKey(signatureKeyPair.privateKey())), accountsLocalDb));
	}
	
	private MessagesEncoder messagesEncoder;
	private KeyPair signatureKeyPair;
	private AccountRef accountRef;
	private Account account;
	
}
