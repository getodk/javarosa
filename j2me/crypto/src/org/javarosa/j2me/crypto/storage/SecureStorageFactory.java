/**
 * 
 */
package org.javarosa.j2me.crypto.storage;

import org.javarosa.core.services.storage.IStorageFactory;
import org.javarosa.core.services.storage.IStorageUtility;
import org.javarosa.j2me.crypto.util.CryptoSession;
import org.javarosa.j2me.storage.rms.RMSStorageUtilityIndexed;
import org.javarosa.j2me.storage.rms.Secure;

/**
 * @author ctsims
 *
 */
public class SecureStorageFactory implements IStorageFactory {
	
	CryptoSession session;
	
	/**
	 * Creates a new storage factory which will create secure storage
	 * when necessary for "Secure" tagged data models.
	 *   
	 * @param session A crypto session which will be attached to the storage.
	 * Doesn't need to be logged in or otherwise initialized.
	 * 
	 * The session will be handed out to lots of in-memory objects, so the passer
	 * should be sure to retain a handle to it in order to modify it during runtime.
	 */
	public SecureStorageFactory(CryptoSession session) {
		this.session = session;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.IStorageFactory#newStorage(java.lang.String, java.lang.Class)
	 */
	public IStorageUtility newStorage(String name, Class type) {
		if(Secure.class.isAssignableFrom(type)) {
			return new RMSStorageUtilityIndexed(name, type, true, new EncryptedRMSFactory(session));
		} else {
			return new RMSStorageUtilityIndexed(name, type);
		}
	}

}
