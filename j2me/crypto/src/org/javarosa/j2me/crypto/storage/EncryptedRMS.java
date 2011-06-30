package org.javarosa.j2me.crypto.storage;

import javax.microedition.rms.RecordStoreException;

import org.bouncycastle.crypto.BufferedBlockCipher;
import org.javarosa.j2me.crypto.util.CryptUtil;
import org.javarosa.j2me.crypto.util.CryptoSession;
import org.javarosa.j2me.storage.rms.raw.RMS;

/**
 * A simple wrapper around an RMS RecordStore that handles common exceptions and
 * provides extra services like automatically opening/closing the RecordStore to
 * free up space.
 */
public final class EncryptedRMS  extends RMS {
	
	BufferedBlockCipher encrypter;
	BufferedBlockCipher decrypter;
	
	CryptoSession session;

	/**
	 * Open/create an RMS and wrap it
	 * 
	 * @param name
	 *            name of the RMS
	 * @param create
	 *            if true, create the RMS if it does not exist
	 * @param key 
	 *            A reference to the crypto session which will
	 *            be used to provide the appropriate information
	 *            needed to handle secure data. 
	 * @throws RecordStoreException
	 *             any exception from openRecordStore() is passed on
	 *             transparently
	 */
	protected EncryptedRMS(String name, boolean create, CryptoSession session) throws RecordStoreException {
		super(name, create);
		
		this.session = session;
	}
	/**
	 * Simple wrapper for RecordStore.addRecord().
	 * 
	 * Optionally, if, on first attempt, RecordStore is full, it will
	 * close/reopen the record store to free up any available space, then try
	 * once more. (This may have a hefty performance penalty)
	 * 
	 * @param data
	 *            record to add
	 * @param tryHard
	 *            if true, will close/reopen the record store on a 'full' error
	 *            and try again
	 * @return id of added record; -1 if full and no record was added
	 */
	public int addRecord(byte[] data, boolean tryHard) {
		//encrypt first
		data = CryptUtil.encrypt(data, encrypter());
		
		//pass down
		return super.addRecord(data, tryHard);
	}

	public boolean updateRecord(int id, byte[] data, boolean tryHard) {
		//encrypt first
		data = CryptUtil.encrypt(data, encrypter());
		return super.updateRecord(id, data, tryHard);
	}

	/**
	 * Return the byte data for a record.
	 * 
	 * @param id
	 *            record ID
	 * @return byte array of record's data; null if no record exists for that ID
	 */
	public byte[] readRecord(int id) {
		//decrypt super
		byte[] data = super.readRecord(id);
		return CryptUtil.decrypt(data, decrypter());
	}
	
	//For now, we'll use one set of ciphers per secure RMS. We'll need to revisit
	//this when it's time to implement logging out, but this will allow
	//for multiple objects to work simultaneously.
	
	private BufferedBlockCipher encrypter() {
		if(encrypter == null) {
			encrypter = session.getEncrypter();
		}
		return encrypter;
	}
	
	private BufferedBlockCipher decrypter() {
		if(decrypter == null) {
			decrypter = session.getDecrypter();
		}
		return decrypter;
	}
}