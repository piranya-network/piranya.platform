package network.piranya.platform.node.core.networks.index.client;

import network.piranya.platform.node.accounts.Account;
import network.piranya.platform.node.api.networking.nodes.Message;

public class SigninInfo extends Message {
	
	private final Account account;
	public Account account() {
		return account;
	}
	
	private final boolean isRefreshZone;
	public boolean isRefreshZone() {
		return isRefreshZone;
	}
	
	/*
	private final AccountingClusterSpec accountingClusterSpec;
	public AccountingClusterSpec accountingClusterSpec() {
		return accountingClusterSpec;
	}
	
	private final long accountingCyclePeriod;
	public long accountingCyclePeriod() {
		return accountingCyclePeriod;
	}
	
	private final long currentAccountingCycleTime;
	public long currentAccountingCycleTime() {
		return currentAccountingCycleTime;
	}
	*/
	
	
	public SigninInfo(Account account, boolean refreshZone) {
		this.account = account;
		this.isRefreshZone = refreshZone;
	}
	
}
