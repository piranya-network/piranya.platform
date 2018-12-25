package network.piranya.platform.node.core.execution.analytics;

import java.math.BigDecimal;

public class FillsStats {
	
	private final int count;
	public int getCount() {
		return count;
	}
	
	private final BigDecimal totalSize;
	public BigDecimal getTotalSize() {
		return totalSize;
	}
	
	public FillsStats(int count, BigDecimal totalSize) {
		this.count = count;
		this.totalSize = totalSize;
	}
	
}
