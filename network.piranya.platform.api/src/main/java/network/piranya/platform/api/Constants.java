package network.piranya.platform.api;

public abstract class Constants {
	
	public static abstract class LP {
		
		public static final String LP_SEPARATOR = ">";
		
		private LP() {}
	}
	
	public static abstract class Bot {
		
		public static final String BOT_SEPARATOR = ":";
		
		private Bot() {}
	}
	
	public static abstract class Instrument {
		
		public static final String BASE_QUOTE_SEPARATOR = "/";
		
		private Instrument() {}
	}
	
	
	private Constants() {}
	
}
