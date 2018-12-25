package network.piranya.platform.node.accounts;

import network.piranya.platform.api.exceptions.InvalidOperationException;

public class Wallet {
	
	private final long id;
	public long id() {
		return id;
	}
	
	private final long balance;
	public long balance() {
		return balance;
	}
	
	public Wallet(long id, long balance) {
		this.id = id;
		this.balance = balance;
	}
	
	
	public Wallet deposit(long amount) {
		return new Wallet(id(), balance() + amount);
	}
	
	public Wallet withdraw(long amount) {
		if (balance() - amount < 0L) {
			throw new InvalidOperationException(String.format("Wallet [%s] current balance of %s is not enough to withdraw %s", id(), balance(), amount));
		}
		return new Wallet(id(), balance() - amount);
	}
	
	
	public static final long DEFAULT_WALLET_ID = 0L;
	
}
