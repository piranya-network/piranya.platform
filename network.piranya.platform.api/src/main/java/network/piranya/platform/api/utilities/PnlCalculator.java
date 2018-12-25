package network.piranya.platform.api.utilities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import network.piranya.platform.api.models.trading.Asset;
import network.piranya.platform.api.models.trading.filling.Fill;

public class PnlCalculator {
	
	public PnlCalculator acceptFill(Fill fill) {
		return acceptFill(fill.price(), fill.size());
	}
	
	public PnlCalculator acceptFill(BigDecimal price, BigDecimal size) {
		boolean isLong = size.doubleValue() > 0.0;
		BigDecimal remainingSize = size.abs();
		List<FillEntry> removed = new ArrayList<>();
		BigDecimal realizedSize = BigDecimal.ZERO;
		BigDecimal pnl = BigDecimal.ZERO;
		
		for (int i = 0; i < unrealizedFills.size() && remainingSize.doubleValue() != 0.0 && unrealizedFills.get(i).isLong != isLong; i++) {
			FillEntry f = unrealizedFills.get(i);
			BigDecimal matchSize;
			if (f.size.compareTo(remainingSize) > 0) {
				matchSize = remainingSize;
				f.size = f.size.subtract(remainingSize);
				remainingSize = BigDecimal.ZERO;
			} else {
				matchSize = f.size;
				remainingSize = remainingSize.subtract(matchSize);
				removed.add(f);
			}
			
			BigDecimal matchPnl = isLong ? f.price.subtract(price) : price.subtract(f.price);
			pnl = pnl.multiply(realizedSize).add(matchPnl.multiply(matchSize)).divide(realizedSize.add(matchSize), RoundingMode.HALF_UP);
			realizedSize = realizedSize.add(matchSize);
		}
		unrealizedFills.removeAll(removed);
		
		if (remainingSize.doubleValue() != 0.0) {
			unrealizedFills.add(new FillEntry(remainingSize, price, isLong));
		}
		
		if (realizedSize.doubleValue() == 0.0 && this.realizedSize.doubleValue() == 0.0) {
			this.realizedPnl = BigDecimal.ZERO;
		} else {
			this.realizedPnl = this.realizedPnl.multiply(this.realizedSize).add(pnl.multiply(realizedSize)).divide(this.realizedSize.add(realizedSize), RoundingMode.HALF_UP);
			this.realizedSize = this.realizedSize.add(realizedSize);
		}
		this.isUnrealizedStatsReset = true;
		
		return this;
	}
	
	private BigDecimal realizedPnl = BigDecimal.ZERO;
	private BigDecimal realizedSize = BigDecimal.ZERO;
	
	public BigDecimal realizedPnl() {
		return realizedPnl;
	}
	
	public BigDecimal realizedSize() {
		return realizedSize;
	}
	
	public BigDecimal openSize() {
		if (unrealizedSize != null && !isUnrealizedStatsReset) {
			return unrealizedSize;
		}
		
		calcUnrealizedStats();
		return unrealizedSize;
	}
	
	public BigDecimal openPrice() {
		if (!isUnrealizedStatsReset) {
			return openPrice;
		}
		
		calcUnrealizedStats();
		return this.openPrice;
	}
	
	protected void calcUnrealizedStats() {
		BigDecimal totalUnrealizedSize = BigDecimal.ZERO;
		BigDecimal totalUnrealizedPrice = BigDecimal.ZERO;
		for (FillEntry f : this.unrealizedFills) {
			totalUnrealizedSize = totalUnrealizedSize.add(f.size);
			totalUnrealizedPrice = totalUnrealizedPrice.add(f.price.multiply(f.size));
		}
		
		this.openPrice = totalUnrealizedSize.doubleValue() != 0.0 ? totalUnrealizedPrice.divide(totalUnrealizedSize, RoundingMode.HALF_UP) : BigDecimal.ZERO;
		this.unrealizedSize = this.unrealizedFills.isEmpty() || this.unrealizedFills.get(0).isLong ? totalUnrealizedSize : totalUnrealizedSize.negate();
		this.isUnrealizedStatsReset = false;
	}
	
	private BigDecimal unrealizedSize;
	private BigDecimal openPrice;
	private boolean isUnrealizedStatsReset;
	
	public BigDecimal pnlIn(Asset currency/*consider requiring context object as a parameter*/) {
		return null;
	}
	
	private List<FillEntry> unrealizedFills = new ArrayList<>();
	
	
	private static class FillEntry {
		
		public BigDecimal size;
		public final BigDecimal price;
		public final boolean isLong;

		public FillEntry(BigDecimal size, BigDecimal price, boolean isLong) {
			this.size = size;
			this.price = price;
			this.isLong = isLong;
		}
	}
	
}
