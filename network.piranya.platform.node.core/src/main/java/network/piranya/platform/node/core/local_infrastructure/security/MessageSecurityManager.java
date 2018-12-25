package network.piranya.platform.node.core.local_infrastructure.security;

import org.bouncycastle.util.Arrays;

import network.piranya.platform.api.accounting.AccountRef;
import network.piranya.platform.api.accounting.NetworkCredentials;
import network.piranya.platform.api.exceptions.SecurityException;
import network.piranya.platform.node.accounts.Account;
import network.piranya.platform.node.api.local_infrastructure.storage.specific.AccountsLocalDb;
import network.piranya.platform.node.utilities.CryptoUtils;

public class MessageSecurityManager {
	
	public AccountRef getSenderAccountAndVerifySignature(byte[] message) throws SecurityException {
		byte[] accountKey = Arrays.copyOfRange(message, 0, 32);
		byte[] signature = Arrays.copyOfRange(message, 32, 32 + 64);
		byte[] data = Arrays.copyOfRange(message, 32 + 64, message.length);
		
		AccountRef accountRef = new AccountRef(accountKey);
		Account account = accountsLocalDb().findAccount(accountRef).orElseThrow(() -> new SecurityException(String.format("Signing Account '%s' not found", accountRef)));
		if (!cryptoUtils().verify(account.security().publicSigningKey().data(), cryptoUtils().digest(data), signature)) {
			throw new SecurityException(String.format("Signature is invalid for account '%s'", accountRef));
		}
		
		return accountRef;
	}
	
	public byte[] generateSignedMessage(byte[] messageData) {
		return Arrays.concatenate(credentials().accountRef().bytes(), cryptoUtils().sign(credentials().privateKey().data(), cryptoUtils().digest(messageData)), messageData);
	}
	
	public byte[] messageData(byte[] message) {
		return Arrays.copyOfRange(message, 32 + 64, message.length);
	}
	
	
	public MessageSecurityManager(NetworkCredentials credentials, AccountsLocalDb accountsLocalDb) {
		this.credentials = credentials;
		this.accountsLocalDb = accountsLocalDb;
		// TODO osgi classpath
		this.cryptoUtils = CryptoUtils.instance();
		//this.cryptoUtils = null;
	}
	
	private final NetworkCredentials credentials;
	protected NetworkCredentials credentials() {
		return credentials;
	}
	
	private final AccountsLocalDb accountsLocalDb;
	protected AccountsLocalDb accountsLocalDb() {
		return accountsLocalDb;
	}
	
	private final CryptoUtils cryptoUtils;
	protected CryptoUtils cryptoUtils() {
		return cryptoUtils;
	}
	
}
