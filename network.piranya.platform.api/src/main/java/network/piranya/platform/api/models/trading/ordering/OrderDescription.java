package network.piranya.platform.api.models.trading.ordering;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import network.piranya.platform.api.lang.Optional;

public class OrderDescription {
	
	private final Optional<String> tradeId;
	public Optional<String> tradeId() {
		return tradeId;
	}
	
	private final Optional<String> userRefId;
	public Optional<String> userRefId() {
		return userRefId;
	}
	
	private final Optional<String> orderTypeId;
	public Optional<String> orderTypeId() {
		return orderTypeId;
	}
	
	private final Optional<String> comments;
	public Optional<String> comments() {
		return comments;
	}
	
	private final Set<String> tags;
	public Set<String> tags() {
		return tags;
	}
	
	
	public OrderDescription(Optional<String> tradeId, Optional<String> userRefId, Optional<String> orderTypeId, Optional<String> comments, Set<String> tags) {
		this.tradeId = tradeId;
		this.userRefId = userRefId;
		this.orderTypeId = orderTypeId;
		this.comments = comments;
		this.tags = tags;
	}
	
	public OrderDescription() {
		this(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), new HashSet<>());
	}
	
	public OrderDescription(Optional<String> userRefId) {
		this(Optional.empty(), userRefId, Optional.empty(), Optional.empty(), new HashSet<>());
	}
	
	public static OrderDescription withTags(String... tags) {
		return new OrderDescription(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), new HashSet<>(Arrays.asList(tags)));
	}
	
	public static OrderDescription forTrade(String tradeId) {
		return new OrderDescription(Optional.of(tradeId), Optional.empty(), Optional.empty(), Optional.empty(), new HashSet<>());
	}
	
}
