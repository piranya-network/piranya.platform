package network.piranya.platform.api.crypto;

public class KeyPair {
	
	private final byte[] privateKey;
	public byte[] privateKey() {
		return privateKey;
	}
	
	private final byte[] publicKey;
	public byte[] publicKey() {
		return publicKey;
	}
	
	public KeyPair(byte[] privateKey, byte[] publicKey) {
		this.privateKey = privateKey;
		this.publicKey = publicKey;
	}
	
}
