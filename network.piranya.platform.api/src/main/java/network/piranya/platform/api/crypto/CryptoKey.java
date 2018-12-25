package network.piranya.platform.api.crypto;

public class CryptoKey {
	
	private final byte[] data;
	public byte[] data() {
		return data;
	}
	
	public CryptoKey(byte[] data) {
		this.data = data;
	}
	
}
