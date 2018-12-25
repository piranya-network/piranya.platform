package network.piranya.platform.node.core.local_infrastructure.storage.specific;

import static network.piranya.platform.node.utilities.CollectionUtils.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import network.piranya.platform.node.utilities.TimeService;
import network.piranya.platform.node.api.local_infrastructure.storage.KeyValueDb;
import network.piranya.platform.node.api.local_infrastructure.storage.LocalStorage;
import network.piranya.platform.node.api.local_infrastructure.storage.specific.AccountsLocalDb;
import network.piranya.platform.node.api.networking.nodes.NodeContacts;
import network.piranya.platform.api.accounting.AccountRef;
import network.piranya.platform.api.exceptions.PiranyaException;
import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.node.accounts.Account;
import network.piranya.platform.node.accounts.activity.AccountActivity;
import network.piranya.platform.node.accounts.security.CertificationInfo;

/// optimization needed long-mid term, but fine for now, as long as accounts amount is in the range of 10k.
public class AccountsLocalDbImpl implements AccountsLocalDb {
	
	@Override
	public Optional<Account> findAccount(AccountRef accountId) {
		Account account = accountsMap().get(accountId);
		return account != null ? Optional.of(account) : Optional.empty();
	}
	
	@Override
	public Account getAccount(AccountRef accountId) {
		return findAccount(accountId).get();
	}
	
	@Override
	public void putAccount(Account account) {
		List<Account> accounts = new ArrayList<>();
		accounts.add(account);
		putAccounts(accounts);
	}
	
	@Override
	public void putAccounts(List<Account> accounts) {
		Map<ByteBuffer, ByteBuffer> puts = new HashMap<>();
		for (Account account : accounts) {
			ByteBuffer accountIdKeyBuffer = ByteBuffer.allocateDirect(2 + account.accountId().bytes().length);
			accountIdKeyBuffer.clear();
			accountIdKeyBuffer.putChar(ACCOUNT_PREFIX);
			accountIdKeyBuffer.put(account.accountId().bytes());
			
			//ByteBuffer accountBytes = ByteBuffer.wrap(serializeAccount(account));
			ByteBuffer accountBytes = ByteBuffer.allocateDirect(100 * 1024);
			byte[] serializedAccount = serializeAccount(account);
			accountBytes.put(serializedAccount, 0, serializedAccount.length);
			puts.put(accountIdKeyBuffer, accountBytes);
			
			if (account.lastUpdateTime() > lastUpdateTime()) {
				setLastUpdateTime(account.lastUpdateTime());
			}
		}
		
		accountsDb().put(puts);
		
		foreach(accounts, a -> accountsMap().put(a.accountId(), a));
	}
	
	@Override
	public Collection<Account> allAccounts() {
		return accountsMap().values();
	}
	
	@Override
	public List<Account> indexNodes() {
		return filter(accountsMap().values(), account -> account.isCertified(CertificationInfo.INDEX_NODE));
	}
	
	@Override
	public Iterator<Account> accountsIterator(Predicate<Account> predicate) {
		return filter(accountsMap().values(), predicate).iterator();
	}
	
	@Override
	public void updateAddressIfNeeded(AccountRef accountId, NodeContacts currentContacts) {
		Account account = accountsMap().get(accountId);
		if (account != null) {
			if (!find(account.accessNodes(), contacts -> contacts.equals(currentContacts)).isPresent()) {
				putAccount(account.updateActivity(account.activity().updateAccessPoint(new AccountActivity.AccessPoint(currentContacts, TimeService.now()))));
			}
		}
	}
	
	@Override
	public void recordFill(AccountRef accountId, long fillVolume) {
		throw new RuntimeException("NotImplemented");
	}
	
	
	protected byte[] serializeAccount(Account account) {
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	        LinkedBuffer buffer = LinkedBuffer.allocate();
	        ProtobufIOUtil.writeTo(outputStream, account, accountSerializationSchema, buffer);
	        return outputStream.toByteArray();
		} catch (Throwable ex) {
			throw new PiranyaException(ex);
		}
	}
	
	protected Account dezerializeAccount(byte[] accountBytes) {
		try {
			ByteArrayInputStream inputStream = new ByteArrayInputStream(accountBytes);
			Account account = accountSerializationSchema.newMessage();
			ProtobufIOUtil.mergeFrom(inputStream, account, accountSerializationSchema);
			return account;
		} catch (Throwable ex) {
			throw new PiranyaException(ex);
		}
	}
	
	private final Schema<Account> accountSerializationSchema = RuntimeSchema.getSchema(Account.class);
	
	
	public AccountsLocalDbImpl(LocalStorage localStorage) {
		this.accountsDb = localStorage.keyValueDb(ACCOUNTS_DB_ID, new KeyValueDb.Config(), this);
		
		accountsDb().iterate(new byte[] { ACCOUNT_PREFIX }, (key, value) -> {
			byte[] accountBytes = new byte[value.remaining()];
			value.get(accountBytes);
			Account account = dezerializeAccount(accountBytes);
			accountsMap().put(account.accountId(), account);
			
			if (account.lastUpdateTime() > lastUpdateTime()) {
				setLastUpdateTime(account.lastUpdateTime());
			}
		});
	}
	
	private final KeyValueDb accountsDb;
	protected KeyValueDb accountsDb() {
		return accountsDb;
	}

	@Override
	public long lastUpdateTime() {
		return lastUpdateTime;
	}
	protected void setLastUpdateTime(long lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}
	private long lastUpdateTime = 0L;
	
	private final ConcurrentMap<AccountRef, Account> accountsMap = new ConcurrentHashMap<>();
	protected ConcurrentMap<AccountRef, Account> accountsMap() {
		return accountsMap;
	}
	
	
	private static final String ACCOUNTS_DB_ID = "accounts-db";
	private static final char ACCOUNT_PREFIX = 'A';
	
}
