package network.piranya.platform.api.accounting;

import java.util.Arrays;

public class AccountRef {
	
	private final byte[] key;
	public byte[] key() {
		return key;
	}
	
	public AccountRef(byte[] key) {
		this.key = key;
	}
	
	
	public byte[] bytes() {
		return key();
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(key());
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof AccountRef) {
			return Arrays.equals(key(), ((AccountRef)o).key());
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return bytesToHex(key());
	}

	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
}
