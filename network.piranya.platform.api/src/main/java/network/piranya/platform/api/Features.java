package network.piranya.platform.api;

public abstract class Features {
	
	public static abstract class Bot {
		
		public static final String TRADING_BOT = "trading-bot";
		
		private Bot() { }
	}
	
	public static abstract class LP {
		
		public static final String REPLAYER_LP = "replayer-lp";
		
		private LP() { }
	}
	
	public static abstract class Commands {
		
		public static final String EDIT_PARAMS = "edit-params";
		
		private Commands() { }
	}
	
	
	private Features() { }
	
}
