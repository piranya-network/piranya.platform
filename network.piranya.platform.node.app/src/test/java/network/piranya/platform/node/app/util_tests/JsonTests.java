package network.piranya.platform.node.app.util_tests;

import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.Test;

import io.vertx.core.json.Json;
import network.piranya.platform.api.extension_models.app.ui.View;

public class JsonTests {

	@Test
	public void testViews() {
		Account account1 = new Account("A1", "Trading1", new BigDecimal("100.4"));
		Account account2 = new Account("A2", "Trading2", new BigDecimal("50.4"));
		
		String accountStr = Json.encode(account1);
		Account dAccount1 = Json.decodeValue(accountStr, Account.class);
		assertEquals(account1.getId(), dAccount1.getId());
		assertEquals(account1.getName(), dAccount1.getName());
		assertEquals(account1.getBalance(), dAccount1.getBalance());
		
		View view1 = new View("account", "A1", account1);
		assertEquals("{\"viewType\":\"account\",\"id\":\"A1\",\"data\":{\"id\":\"A1\",\"name\":\"Trading1\",\"balance\":100.4}}", Json.encode(view1));
		
		Json.encode(new Account[] { account1, account2 });
	}
	
	
	public static class Account {
		
		private String id;
		public String getId() {
			return id;
		}
		
		private String name;
		public String getName() {
			return name;
		}
		
		private BigDecimal balance;
		public BigDecimal getBalance() {
			return balance;
		}
		
		
		protected Account() {
		}
		
		public Account(String id, String name, BigDecimal balance) {
			this.id = id;
			this.name = name;
			this.balance = balance;
		}
	}
	
}
