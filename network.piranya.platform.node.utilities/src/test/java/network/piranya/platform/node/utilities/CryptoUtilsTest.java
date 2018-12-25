package network.piranya.platform.node.utilities;

import static org.junit.Assert.*;

import org.junit.Test;

import network.piranya.platform.api.crypto.KeyPair;

public class CryptoUtilsTest {
	
	CryptoUtils cryptoUtils = CryptoUtils.instance();
	
	@Test
	public void testSha3_256() {
		assertEquals("a7ffc6f8bf1ed76651c14756a061d662f580ff4de43b49fa82d80a4b80f8434a", cryptoUtils.encode(cryptoUtils.digest("".getBytes())));
	}
	
	@Test
	public void testValidSignature() {
		KeyPair keyPair = cryptoUtils.generateSignatureKeyPair();
		
		byte[] dataHash = cryptoUtils.digest("test_data".getBytes());
		byte[] signature = cryptoUtils.sign(keyPair.privateKey(), dataHash);
		assertTrue(cryptoUtils.verify(keyPair.publicKey(), dataHash, signature));
	}
	
	@Test
	public void testInvalidSignature() {
		KeyPair signingKeyPair = cryptoUtils.generateSignatureKeyPair();
		KeyPair verifyingKeyPair = cryptoUtils.generateSignatureKeyPair();
		
		byte[] dataHash = cryptoUtils.digest("test_data".getBytes());
		byte[] signature = cryptoUtils.sign(signingKeyPair.privateKey(), dataHash);
		assertFalse(cryptoUtils.verify(verifyingKeyPair.publicKey(), dataHash, signature));
	}
	
	@Test
	public void testSymmetricEncryption() {
		byte[] nonce = cryptoUtils.symetricEncryptionNonce();
		byte[] digestedPassword = cryptoUtils.digest("password".getBytes());
		
		byte[] cipher = cryptoUtils.encryptSymmetrically(digestedPassword, nonce, "test_data".getBytes());
		assertEquals("test_data", new String(cryptoUtils.decryptSymmetrically(digestedPassword, nonce, cipher)));
	}
	
	@Test(expected = CryptoUtils.CryptoException.class)
	public void testInvalidSymmetricEncryption() {
		byte[] nonce = cryptoUtils.symetricEncryptionNonce();
		byte[] digestedPassword = cryptoUtils.digest("password".getBytes());
		byte[] digestedInvalidPassword = cryptoUtils.digest("password2".getBytes());
		
		byte[] cipher = cryptoUtils.encryptSymmetrically(digestedPassword, nonce, "test_data".getBytes());
		cryptoUtils.decryptSymmetrically(digestedInvalidPassword, nonce, cipher);
	}
	
	@Test
	public void testSealedBoxEncryption() {
		KeyPair keyPair = cryptoUtils.generateSealedBoxKeyPair();
		
		byte[] cipher = cryptoUtils.encryptSealedBox(keyPair.publicKey(), "test_data".getBytes());
		assertEquals("test_data", new String(cryptoUtils.deecryptSealedBox(keyPair.privateKey(), keyPair.publicKey(), cipher)));
	}
	
}
