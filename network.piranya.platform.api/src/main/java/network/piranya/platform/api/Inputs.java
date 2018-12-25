package network.piranya.platform.api;

public abstract class Inputs {
	
	public static final String DISPLAY_NAME = "display_name";
	
	
	public static abstract class Trading {
		
		public static final String INSTRUMENT = "symbol";
		
		public static final String INSTRUMENTS = "symbols";
		
		public static final String AUTO_TRADE = "auto_trade";
		
		public static final String LP_ID = "lp_id";
		
		private Trading() { }
	}
	
	
	private Inputs() { }
	
}
