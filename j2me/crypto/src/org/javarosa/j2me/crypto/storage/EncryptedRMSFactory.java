/**
 * 
 */
package org.javarosa.j2me.crypto.storage;

import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotFoundException;

import org.javarosa.j2me.crypto.util.CryptoSession;
import org.javarosa.j2me.storage.rms.raw.RMS;
import org.javarosa.j2me.storage.rms.raw.RMSFactory;

/**
 * @author ctsims
 *
 */
public final class EncryptedRMSFactory extends RMSFactory {
	
	private CryptoSession session;
	
	public EncryptedRMSFactory(CryptoSession session) {
		this.session = session;
	}
	
	public final RMS getIndexRMS(String name, boolean create) throws RecordStoreNotFoundException, RecordStoreException {
		return new RMS(name, create);
	}
	
	public final RMS getDataRMS(String name, boolean create) throws RecordStoreNotFoundException, RecordStoreException {
		return new EncryptedRMS(name, create, session);
	}
}
