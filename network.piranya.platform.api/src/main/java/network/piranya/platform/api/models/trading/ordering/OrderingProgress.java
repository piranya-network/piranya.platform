package network.piranya.platform.api.models.trading.ordering;

import java.math.BigDecimal;
import network.piranya.platform.api.lang.Optional;

public class OrderingProgress {
	
	private final String statusId;
	public String statusId() {
		return statusId;
	}
	
	private final String descirption;
	public String descirption() {
		return descirption;
	}
	
	private final BigDecimal filledSize;
	public BigDecimal filledSize() {
		return filledSize;
	}
	
	private final Optional<String> details;
	public Optional<String> details() {
		return details;
	}
	
	public OrderingProgress(String statusId, String descirption, BigDecimal filledSize, Optional<String> details) {
		this.statusId = statusId;
		this.descirption = descirption;
		this.filledSize = filledSize;
		this.details = details;
	}
	
	public OrderingProgress() {
		this(PLACED_AT_LP_STATUS, "Placed", new BigDecimal(0), Optional.empty());
	}
	
	public OrderingProgress(String statusId, String descirption, BigDecimal filledSize) {
		this(statusId, descirption, filledSize, Optional.empty());
	}
	
	public OrderingProgress(String statusId, String descirption) {
		this(statusId, descirption, new BigDecimal(0), Optional.empty());
	}
	
	
	public static final String EXECUTING_STATUS = "EXECUTING";
	public static final String PLACED_AT_LP_STATUS = "PLACED_AT_LP";
	public static final String PARTIALLY_FILLED_STATUS = "PARTIALLY_FILLED";
	public static final String CANCELLING_STATUS = "CANCELLING";
	public static final String CANCELLED_STATUS = "CANCELLED";
	
}
