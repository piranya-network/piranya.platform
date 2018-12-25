package network.piranya.platform.node.api.local_infrastructure.storage.specific;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import network.piranya.platform.api.accounting.AccountRef;
import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.node.accounts.Account;
import network.piranya.platform.node.api.networking.nodes.NodeContacts;

public interface AccountsLocalDb {

	long lastUpdateTime();

	void recordFill(AccountRef accountId, long fillVolume);

	void updateAddressIfNeeded(AccountRef accountId, NodeContacts currentContacts);

	Iterator<Account> accountsIterator(Predicate<Account> predicate);

	List<Account> indexNodes();

	Collection<Account> allAccounts();

	void putAccounts(List<Account> accounts);

	void putAccount(Account account);

	Account getAccount(AccountRef accountId);

	Optional<Account> findAccount(AccountRef accountId);
	
}
