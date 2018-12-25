package network.piranya.platform.api.extension_models.execution.liquidity;

import network.piranya.platform.api.lang.Optional;

public class LpConnectionStatus {
	
	public static enum Status { ONLINE, OFFLINE }
	
	
	private final Status status;
	public Status status() {
		return status;
	}
	
	private final Optional<String> description;
	public Optional<String> description() {
		return description;
	}
	
	public LpConnectionStatus(Status status) {
		this.status = status;
		this.description = Optional.empty();
	}
	
	public LpConnectionStatus(Status status, String description) {
		this.status = status;
		this.description = Optional.of(description);
	}
	
}
