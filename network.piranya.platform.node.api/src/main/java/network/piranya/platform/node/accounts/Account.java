package network.piranya.platform.node.accounts;

import static network.piranya.platform.node.utilities.CollectionUtils.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import network.piranya.platform.api.accounting.AccountRef;
import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.node.accounts.activity.AccountActivity;
import network.piranya.platform.node.accounts.security.AccountSecurity;
import network.piranya.platform.node.accounts.security.CertificationInfo;
import network.piranya.platform.node.api.networking.nodes.NodeContacts;
import network.piranya.platform.node.api.networking.nodes.Zone;
import network.piranya.platform.node.utilities.TimeService;

public class Account {
	
	public Wallet wallet(long id) {
		return find(wallets(), w -> w.id() == id).orElseThrow(() -> new WalletNotFoundException(id));
	}
	
	public Wallet defaultWallet() {
		return wallet(Wallet.DEFAULT_WALLET_ID);
	}
	
	public Account updateWallet(Wallet wallet) {
		List<Wallet> wallets = new ArrayList<>(wallets());
		Optional<Integer> currentIndex = find(wallets, w -> w.id() == wallet.id()).map(oldWallet -> wallets.indexOf(oldWallet));
		if (currentIndex.isPresent()) {
			wallets.remove(currentIndex.get().intValue());
			wallets.add(currentIndex.get(), wallet);
		} else {
			wallets.add(wallet);
		}
		
		return new Account(accountId(), wallets, security(), accountingZone(), version() + 1, TimeService.now(), activity());
	}
	
	public Account updateWallet(long id, Function<Wallet, Wallet> updater) {
		Wallet currentWallet = wallet(id);
		return updateWallet(updater.apply(currentWallet));
	}
	
	public boolean isCertified(CertificationInfo c) {
		return find(security().certifications(), certification -> c.equals(certification)).isPresent();
	}
	
	
	public Account(AccountRef accountId, List<Wallet> wallets, AccountSecurity security, Zone accountingZone, long version, long lastUpdateTime, AccountActivity activity) {
		this.accountId = accountId;
		this.accountingZone = accountingZone;
		this.version = version;
		this.lastUpdateTime = lastUpdateTime;
		this.activity = activity;
		this.security = security;
		this.wallets = Collections.unmodifiableList(wallets);
	}
	
	private final AccountRef accountId;
	public AccountRef accountId() {
		return accountId;
	}
	
	private final List<Wallet> wallets;
	public List<Wallet> wallets() {
		return wallets;
	}
	
	private final Zone accountingZone;
	public Zone accountingZone() {
		return accountingZone;
	}
	
	public List<NodeContacts> accessNodes() {
		return map(activity().accessPoints(), p -> p.nodeContacts());
	}
	
	private final long version;
	public long version() {
		return version;
	}
	
	private final long lastUpdateTime;
	public long lastUpdateTime() {
		return lastUpdateTime;
	}
	
	private final AccountSecurity security;
	public AccountSecurity security() {
		return security;
	}
	
	private final AccountActivity activity;
	public AccountActivity activity() {
		return activity;
	}
	
	
	public Account updateActivity(AccountActivity activity) {
		return new Account(accountId, wallets, security(), accountingZone, version, lastUpdateTime, activity);
	}
	
	
	public static Account create(AccountRef accountId, Optional<Zone> accountingZone, AccountSecurity security, AccountActivity activity) {
		ArrayList<Wallet> wallets = new ArrayList<>();
		wallets.add(new Wallet(Wallet.DEFAULT_WALLET_ID, 0L));
		
		return new Account(accountId, wallets, security, accountingZone.orElse(Zone.ANY), 0L, TimeService.now(), activity);
	}
	
	public static Account create(AccountRef accountId, Optional<Zone> accountingZone, AccountSecurity security) {
		return create(accountId, accountingZone, security, new AccountActivity());
	}
	
}
