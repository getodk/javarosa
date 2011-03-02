/**
 * 
 */
package org.javarosa.j2me.crypto.util;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.modes.CTSBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;

/**
 * A CryptoSession is used to manage the in memory details
 * of encryption and decryption. It maintains the record
 * of the symmetric key used to secure data, and dispatches
 * ciphers as necessary.
 * 
 * In general, there should really only be one CryptoSession
 * object in memory, and you should maintain a reference to it 
 * in order to manage logging in and out of secure storage.
 * 
 * @author ctsims
 *
 */
public final class CryptoSession {
	
	/**
	 * The big kahuna, the symmetric key which is used to secure
	 * all data. 
	 */
	private byte[] inMemorySecretKey;
	
	/**
	 * Creates a CryptoSession to be used to manage secure data. Does not
	 * require credentials yet, but bear in mind that if private data is
	 * requested, an unhandled exception will be thrown
	 */
	public CryptoSession() {
		
	}
	
	/**
	 * Doesn't do anything yet.
	 */
	public final void logOut() {
		//Not really supported yet, since we can't cut ties with all of the
		//ciphers we've handed out. (possibly use one set of ciphers in the
		//future, although that will limit what can happen at the same time.
	}
	
	/**
	 * Log in according to a Password used to wrap the passed in encrypted
	 * key with a PBE scheme.
	 * 
	 * @param password The password which decrypts the passed key.
	 * @param wrappedKey the bytes of the symmetric key which are 
	 * encrypted by the provided password.
	 *  
	 * @return True if the key was successfully decrypted and secure data
	 * is available. False otherwise.
	 */
	public final boolean logIn(String password, byte[] wrappedKey) {
		byte[] key = CryptUtil.unWrapKey(wrappedKey, password);
		if(key == null) {
			return false;
		} else {
			inMemorySecretKey = key;
			return true;
		}
	}
	
	/**
	 * Directly provide the symmetric key in order to make secure
	 * data available.
	 * 
	 * @param rawKey The symmetric key which should be used to encrypt
	 * and decrypt all data.
	 */
	public final void logIn(byte[] rawKey) {
		inMemorySecretKey = rawKey;
	}
	
	/**
	 * NOTE: Will throw a fatal exception if no symmetric key has been provided
	 * 
	 * @return A cipher which can be used to encrypt secure data.
	 */
	public final BufferedBlockCipher getEncrypter() {		
		if(inMemorySecretKey == null) { throw new RuntimeException("Secure encrypter requested before session is logged in"); }
		
		BufferedBlockCipher encrypter = CryptUtil.getAesCtsCipher();
		
		encrypter.init(true, new KeyParameter(inMemorySecretKey));
		
		return encrypter;
	}
	
	/**
	 * NOTE: Will throw a fatal exception if no symmetric key has been provided
	 * 
	 * @return A cipher which can be used to decrypt secure data. 
	 */
	public final BufferedBlockCipher getDecrypter() {
		if(inMemorySecretKey == null) { throw new RuntimeException("Secure decrypter requested before session is logged in"); } 
		
		BufferedBlockCipher decrypter = CryptUtil.getAesCtsCipher();
		
		decrypter.init(false, new KeyParameter(inMemorySecretKey));
		
		return decrypter;
	}
}
