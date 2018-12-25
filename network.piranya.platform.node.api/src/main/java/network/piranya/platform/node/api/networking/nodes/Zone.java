package network.piranya.platform.node.api.networking.nodes;

public class Zone {
	
	private final int descriptor;
	public int descriptor() {
		return descriptor;
	}
	
	public int threeDigitDescriptor() {
		return descriptor();
	}
	
	
	public boolean isSuitable(Zone other) {
		return descriptor() == other.descriptor() || descriptor() == UNIDENTIFIED || other.descriptor() == UNIDENTIFIED;
	}
	
	
	public Zone(int descriptor) {
		this.descriptor = descriptor;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Zone) {
			return descriptor() == ((Zone)obj).descriptor();
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return descriptor();
	}
	
	
	public static Zone parse(String zoneStr) {
		return new Zone(Integer.parseInt(zoneStr));
	}
	
	public String stringify() {
		return new Integer(descriptor).toString();
	}
	
	
	private static final int UNIDENTIFIED = 0;
	
	public static final Zone ANY = new Zone(UNIDENTIFIED);
	
}
