/**
 * 
 */
package org.javarosa.j2me.crypto.test;

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

import java.util.Random;

import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.encoders.Hex;
import org.javarosa.j2me.crypto.util.CryptUtil;
import org.javarosa.j2me.crypto.util.CryptoSession;

/**
 * @author ctsims
 * 
 */
public class CryptTest extends TestCase {

	public final int NUM_TESTS = 3;

	public CryptTest(String name, TestMethod rTestMethod) {
		super(name, rTestMethod);
	}

	public CryptTest(String name) {
		super(name);
	}

	public CryptTest() {
		super();
	}

	public Test suite() {
		TestSuite aSuite = new TestSuite();

		for (int i = 1; i <= NUM_TESTS; i++) {
			final int testID = i;

			aSuite.addTest(new CryptTest("Crypt Engine Test " + i,
					new TestMethod() {
						public void run(TestCase tc) {
							((CryptTest) tc).testMaster(testID);
						}
					}));
		}

		return aSuite;
	}

	public void testMaster(int testID) {
		// System.out.println("running " + testID);

		switch (testID) {
		case 1:
			testPBEUtil();
			break;
		case 2:
			testCryptSession();
			break;
		case 3: 
			testEdges();
			break;
		}
	}

	public void testPBEUtil() {
		byte[] key = genkey();
		
		byte[] wrapped = CryptUtil.wrapKey(key, "javarosarules");
		
		
		byte[] restored = CryptUtil.unWrapKey(wrapped, "javarosarules");
		
		if(!Arrays.areEqual(key,restored)) {
			fail("PBE did not correctly wrap/unwrap key");
		}
		
	}
	
	public void testCryptSession() {
		byte[] key = genkey();
		
		CryptoSession session = new CryptoSession();
		session.logIn(key);
		
		BufferedBlockCipher rawEncrypt = session.getEncrypter();
		BufferedBlockCipher rawDecrypt = session.getDecrypter();
		
		byte[] payload = Hex.decode("11b11454f4b5160a78801cb8c5dcff50");

		byte[] t1 = CryptUtil.encrypt(payload,rawEncrypt);
		byte[] t2 = CryptUtil.decrypt(t1, rawDecrypt);
		
		if(!Arrays.areEqual(payload, t2)) {
			fail("Encrypt and Decrypt with raw key in CryptSession failed");
		}
		
		CryptoSession pbeSession = new CryptoSession();
		
		byte[] wrapped = CryptUtil.wrapKey(key, "javarosarules");
		pbeSession.logIn("javarosarules", wrapped);
		
		BufferedBlockCipher pbeEncrypt = pbeSession.getEncrypter();
		BufferedBlockCipher pbeDecrypt = pbeSession.getDecrypter();

		
		byte[] t1w = CryptUtil.encrypt(payload, pbeEncrypt);
		if(!Arrays.areEqual(t1, t1w)) {
			fail("Encrypt: PBE and raw key crypto sessions were not equivilant");
		}
		
		byte[] t2w = CryptUtil.decrypt(t1, pbeDecrypt);
		if(!Arrays.areEqual(t2, t2w)) {
			fail("Decrpyt: PBE and raw key crypto sessions were not equivilant");
		}
	}
	public void testEdges() {		
		byte[] key = genkey();
		
		CryptoSession session = new CryptoSession();
		session.logIn(key);
		
		BufferedBlockCipher encrypt = session.getEncrypter();
		BufferedBlockCipher decrypt = session.getDecrypter();

		//Blocks are generally 16 bytes, so hit around those edges. 
		test(1, Hex.decode("11b11454f4b5160a78801cb8c5dcff511b11454f4b5160a78801cb8c5dcff5aa"), encrypt, decrypt);
		test(2, Hex.decode("11b11454f4b5160a78801cb8c5dcff511b11454f4b5160a78801cb8c5dcff5"), encrypt, decrypt);
		test(3, Hex.decode("11b11454f4b5160a78801cb8c5dcff511b11454f4b5160a78801cb8c5dcf"), encrypt, decrypt);
		test(4, Hex.decode("11b11454f4b5160a78801cb8c5dcffcfaa"), encrypt, decrypt);
		test(5, Hex.decode("11b11454f4b5160a78801cb8c5dcffcf"), encrypt, decrypt);
		test(6, Hex.decode("11b11454f4b5160a78801cb8c5dcff"), encrypt, decrypt);
		test(7, Hex.decode("11b114541f4b5160a78801cb8c"), encrypt, decrypt);
		test(8, Hex.decode("11b11454f4b5160a"), encrypt, decrypt);
		test(9, Hex.decode("a78801cb"), encrypt, decrypt);
		test(10, Hex.decode("12"), encrypt, decrypt);
		test(10, Hex.decode("00"), encrypt, decrypt);
		test(11, Hex.decode(""), encrypt, decrypt);
	}
	
	private void test(int n, byte[] payload, BufferedBlockCipher encrypt,  BufferedBlockCipher decrpyt) {
		byte[] t1 = CryptUtil.encrypt(payload,encrypt);
		byte[] t2 = CryptUtil.decrypt(t1, decrpyt);
		
		if(!Arrays.areEqual(payload, t2)) {
			fail("CryptFail: " + n);
		}

	}
	
	private byte[] genkey() {
		Random r = new Random();
		
		byte[] key = new byte[32];
		
		for(int i = 0 ; i < key.length ; ++i ) {
			key[i] = (byte)r.nextInt();
		}
		return key;
	}
}