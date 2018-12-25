package network.piranya.platform.tests.commons.node;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import network.piranya.platform.node.accounts.Account;
import network.piranya.platform.node.api.local_infrastructure.LocalServices;
import network.piranya.platform.node.api.local_infrastructure.storage.specific.AccountsLocalDb;
import network.piranya.platform.node.core.booting.NetworkNode;
import network.piranya.platform.node.utilities.CollectionUtils;
import network.piranya.platform.node.utilities.ReflectionUtils;

public class AccountsFiller {
	
	public AccountsFiller registerAccount(TestAccount account) {
		queuedRegisteredAccounts.add(account);
		return this;
	}
	
	public void fill() {
		List<Account> accountsToAdd = CollectionUtils.map(queuedRegisteredAccounts, a -> a.account());
		queuedRegisteredAccounts.clear();
		
		perform(accountsLocalDb -> accountsLocalDb.putAccounts(accountsToAdd));
	}
	
	
	protected void perform(Consumer<AccountsLocalDb> operation) {
		accountsLocalDbs.forEach(operation);
	}
	
	
	public AccountsFiller(NetworkNode... nodes) {
		for (NetworkNode node : nodes) {
			accountsLocalDbs.add(((LocalServices)ReflectionUtils.getFieldValue(node, NetworkNode.class, "localServices")).localStorage().accountsLocalDb());
		}
	}
	
	private final List<AccountsLocalDb> accountsLocalDbs = new ArrayList<>();
	private final List<TestAccount> queuedRegisteredAccounts = new ArrayList<>();
	
}
