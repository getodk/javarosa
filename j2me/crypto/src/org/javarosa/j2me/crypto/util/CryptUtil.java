/**
 * 
 */
package org.javarosa.j2me.crypto.util;

import java.security.SecureRandom;
import java.util.Date;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.modes.CTSBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.encoders.Hex;
import org.javarosa.core.services.Logger;

/**
 * @author ctsims
 *
 */
public class CryptUtil {
	
	private static final int PBE_KEY_SIZE = 256;

	
    //TODO: Streaming ciphers
	
	private final static byte[] pad = Hex.decode("00000000000000000000000000000000");
	
	public static byte[] encrypt(byte[] input, BufferedBlockCipher cipher) {
		synchronized(cipher) {
			cipher.reset();
			
			byte[] cipherText = new byte[cipher.getOutputSize(input.length + pad.length)];
			
			//Write out the pad
			int outputLen = cipher.processBytes(pad, 0, pad.length, cipherText, 0);
			
			outputLen += cipher.processBytes(input, 0, input.length, cipherText, outputLen);
			
			try {
				cipher.doFinal(cipherText, outputLen);
			} catch(CryptoException e) {
				Logger.die("process", e);
			}
			
			return cipherText;
		}

	}
	
	public static byte[] process(byte[] input, BufferedBlockCipher cipher) {
		
		byte[] cipherText = new byte[cipher.getOutputSize(input.length)];
		
		int outputLen = cipher.processBytes(input, 0, input.length, cipherText, 0);
		
		try {
			cipher.doFinal(cipherText, outputLen);
		} catch(CryptoException e) {
			Logger.die("process", e);
		}
		
		return cipherText;
	}

	
	public static byte[] decrypt(byte[] input, BufferedBlockCipher cipher) {
		
		synchronized(cipher) {
		
		byte[] cipherText = new byte[cipher.getOutputSize(input.length) - pad.length];
		
		int padCopied = 0;
				
		if(input.length -1 >= cipher.getBlockSize() * 2) {
			//If the pad is not relevant for the last two blocks, simply process it out ahead of time
			padCopied = cipher.getUnderlyingCipher().processBlock(input, 0, new byte[pad.length], 0);
		} else {
			//Otherwise, the pad may be relevant for the CTS step, so we'll need to process it 
			//normally
			padCopied = cipher.processBytes(input, 0, pad.length, new byte[pad.length], 0);
		}
		
		int outputLen = cipher.processBytes(input, pad.length, input.length - pad.length, cipherText, 0);
		
		byte[] lastBlock = new byte[input.length - padCopied - outputLen];
		
		try {
			int size = cipher.doFinal(lastBlock, 0);
			int count = 0;
			for(int i = pad.length - padCopied ; i < size ; ++i) {
				cipherText[outputLen + count] = lastBlock[i];
				count++;
			}
		} catch(CryptoException e) {
			Logger.die("process", e);
		}
		
		return cipherText;
		
		}
	}
	
	public static byte[] wrapKey(byte[] secretKey, String password) {
		
		byte[] tempsalt = "TEST".getBytes();
		PBEParametersGenerator generator = new PKCS5S2ParametersGenerator();
		generator.init(PBEParametersGenerator.PKCS5PasswordToBytes(password.toCharArray()), tempsalt, 5);
		
		BlockCipher engine = new AESEngine();
		BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(engine));
		cipher.init(true, generator.generateDerivedParameters(PBE_KEY_SIZE));
		
		byte[] wrapped = process(secretKey, cipher);
		return wrapped;
	}
	
	public static byte[] unWrapKey(byte[] wrapped, String password) {
		byte[] tempsalt = "TEST".getBytes();
		PBEParametersGenerator generator = new PKCS5S2ParametersGenerator();
		generator.init(PBEParametersGenerator.PKCS5PasswordToBytes(password.toCharArray()), tempsalt, 5);
		
		BlockCipher engine = new AESEngine();
		BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(engine));
		cipher.init(false, generator.generateDerivedParameters(PBE_KEY_SIZE));
		
		byte[] unWrapped = process(wrapped, cipher);
		return unWrapped;
	}
}
