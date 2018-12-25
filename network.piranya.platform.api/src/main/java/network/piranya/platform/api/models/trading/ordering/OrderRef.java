package network.piranya.platform.api.models.trading.ordering;

import network.piranya.platform.api.Constants;
import network.piranya.platform.api.models.trading.liquidity.LiquidityProviderRef;

public abstract class OrderRef {
	
	public abstract String refString();
	
	private OrderRef() { }
	
	public static OrderRef parse(String str) {
		if (str.startsWith("X:")) {
			String s = str.substring(2);
			int lpIndex = s.indexOf(Constants.LP.LP_SEPARATOR);
			return new ExternalOrderRef(s.substring(0, lpIndex), new LiquidityProviderRef(s.substring(lpIndex + 1)));
		} else if (str.startsWith("D:")) {
			String s = str.substring(2);
			int lpIndex = s.indexOf(Constants.LP.LP_SEPARATOR);
			return new ExternalOrderRef(s.substring(0, lpIndex), new LiquidityProviderRef(s.substring(lpIndex + 1)));
		} else {
			return new PlatformOrderRef(str);
		}
	}
	
	
	public static class PlatformOrderRef extends OrderRef {
		
		private final String orderId;
		public String orderId() {
			return orderId;
		}
		
		public String refString() { return orderId; }
		
		public PlatformOrderRef(String orderId) {
			this.orderId = orderId;
		}
		
		@Override
		public int hashCode() {
			return orderId.hashCode();
		}
		
		@Override
		public String toString() {
			return orderId;
		}
		
		@Override
		public boolean equals(Object o) {
			return o instanceof PlatformOrderRef && orderId().equals(((PlatformOrderRef)o).orderId());
		}
	}
	
	public static class ExternalOrderRef extends OrderRef {
		
		private final String externalOrderId;
		public String externalOrderId() {
			return externalOrderId;
		}
		
		private final LiquidityProviderRef lpRef;
		public LiquidityProviderRef lpRef() {
			return lpRef;
		}
		
		private final String refString;
		public String refString() { return refString; }
		
		public ExternalOrderRef(String externalOrderId, LiquidityProviderRef lpRef) {
			this.externalOrderId = externalOrderId;
			this.lpRef = lpRef;
			this.refString = String.format("X:%s>%s", externalOrderId, lpRef.liquidityProviderId());
		}
		
		@Override
		public int hashCode() {
			return refString.hashCode();
		}
		
		@Override
		public String toString() {
			return refString;
		}
		
		@Override
		public boolean equals(Object o) {
			return o instanceof DetachedOrderRef
					&& externalOrderId().equals(((DetachedOrderRef)o).externalOrderId())
					&& lpRef().equals(((DetachedOrderRef)o).lpRef());
		}
	}
	
	public static class DetachedOrderRef extends ExternalOrderRef {
		
		private final String refString;
		public String refString() { return refString; }
		
		public DetachedOrderRef(String externalOrderId, LiquidityProviderRef lpRef) {
			super(externalOrderId, lpRef);
			this.refString = String.format("D:%s>%s", externalOrderId(), lpRef().liquidityProviderId());
		}
		
		@Override
		public String toString() {
			return refString;
		}
	}
	
}
