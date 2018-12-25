package network.piranya.platform.node.utilities;

import static org.abstractj.kalium.encoders.Encoder.HEX;

import org.abstractj.kalium.crypto.Aead;
import org.abstractj.kalium.crypto.Random;
import org.abstractj.kalium.crypto.SealedBox;
import org.abstractj.kalium.keys.SigningKey;
import org.abstractj.kalium.keys.VerifyKey;
import org.bouncycastle.jcajce.provider.digest.SHA3;

import network.piranya.platform.api.crypto.KeyPair;

public class CryptoUtils {
	
	public KeyPair generateSignatureKeyPair() {
		byte[] rawKey = random().randomBytes(32);
    	SigningKey key = new SigningKey(rawKey);
    	
    	return new KeyPair(key.toBytes(), key.getVerifyKey().toBytes());
	}
	
	public KeyPair fromPrivate(byte[] privateKey) {
		SigningKey key = new SigningKey(privateKey);
		return new KeyPair(key.toBytes(), key.getVerifyKey().toBytes());
	}
	
	public byte[] sign(byte[] privateKey, byte[] dataHash) {
		SigningKey key = new SigningKey(privateKey);
		return key.sign(dataHash);
	}
	
	public boolean verify(byte[] publicKey, byte[] dataHash, byte[] signature) {
		VerifyKey verifyKey = new VerifyKey(publicKey);
		try {
			return verifyKey.verify(dataHash, signature);
		} catch (Exception ex) {
			return false;
		}
	}
	
	public String encode(byte[] bytes) {
		return HEX.encode(bytes);
	}
	
	public byte[] encode(String data) {
		return HEX.decode(data);
	}
	
	public byte[] digest(byte[] bytes) {
		SHA3.DigestSHA3 digestSHA3 = new SHA3.Digest256();
	    return digestSHA3.digest(bytes);
	}
	
	
	public byte[] symetricEncryptionNonce() {
		return random().randomBytes(8);
	}
	
	public byte[] encryptSymmetrically(byte[] digestedKey, byte[] nonce, byte[] data) {
		Aead aead = new Aead(digestedKey);
		return aead.encrypt(nonce, data, new byte[0]);
	}
	
	public byte[] decryptSymmetrically(byte[] digestedKey, byte[] nonce, byte[] cipher) {
		Aead aead = new Aead(digestedKey);
		try {
			return aead.decrypt(nonce, cipher, new byte[0]);
		} catch (Exception ex) {
			throw new CryptoException(ex);
		}
	}
	
	
	public KeyPair generateSealedBoxKeyPair() {
		byte[] rawKey = random().randomBytes(32);
		org.abstractj.kalium.keys.KeyPair keyPair = new org.abstractj.kalium.keys.KeyPair(rawKey);
    	
    	return new KeyPair(keyPair.getPrivateKey().toBytes(), keyPair.getPublicKey().toBytes());
	}
	
	public byte[] encryptSealedBox(byte[] publicKey, byte[] data) {
		SealedBox sb = new SealedBox(publicKey);
        return sb.encrypt(data);
	}
	
	public byte[] deecryptSealedBox(byte[] privateKey, byte[] publicKey, byte[] cipher) {
		SealedBox sb = new SealedBox(publicKey, privateKey);
        return sb.decrypt(cipher);
	}
	
	
	
	protected Random random() {
		if (this.random == null) {
			this.random = new Random();
		}
		return this.random;
	}
	private Random random;
	
	
	private static final CryptoUtils instance = new CryptoUtils();
	public static CryptoUtils instance() { return instance; }
	
	private CryptoUtils() { }
	
	
	public static class CryptoException extends RuntimeException {
		
		public CryptoException(Exception cause) {
			super(cause.getMessage(), cause);
		}
		
		private static final long serialVersionUID = ("urn:" + CryptoException.class.getName()).hashCode();
	}
	
}
