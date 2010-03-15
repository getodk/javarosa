/**
 * 
 */
package org.javarosa.services.transport;

import org.javarosa.core.api.IModule;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.core.services.PrototypeManager;
import org.javarosa.core.services.storage.IStorageFactory;
import org.javarosa.core.services.storage.IStorageUtility;
import org.javarosa.core.services.storage.StorageManager;
import org.javarosa.core.services.storage.WrappingStorageUtility;
import org.javarosa.j2me.reference.HttpRoot;
import org.javarosa.j2me.storage.rms.RMSStorageUtilityIndexed;
import org.javarosa.services.transport.impl.TransportMessageSerializationWrapper;
import org.javarosa.services.transport.impl.TransportMessageStore;
import org.javarosa.services.transport.impl.binarysms.BinarySMSTransportMessage;
import org.javarosa.services.transport.impl.simplehttp.SimpleHttpTransportMessage;
import org.javarosa.services.transport.impl.sms.SMSTransportMessage;

/**
 * @author ctsims
 *
 */
public class TransportManagerModule implements IModule {

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IModule#registerModule()
	 */
	public void registerModule() {
		String[] prototypes = new String[] { SimpleHttpTransportMessage.class.getName(), SMSTransportMessage.class.getName(), BinarySMSTransportMessage.class.getName(), TransportMessageSerializationWrapper.class.getName()};
		PrototypeManager.registerPrototypes(prototypes);
		IStorageFactory f = new IStorageFactory () {
			public IStorageUtility newStorage(String name, Class type) {
				return new RMSStorageUtilityIndexed(name, type);
			}
		} ;
		StorageManager.registerStorage(TransportMessageStore.Q_STORENAME, new WrappingStorageUtility(TransportMessageStore.Q_STORENAME,new TransportMessageSerializationWrapper(),f));
		StorageManager.registerStorage(TransportMessageStore.RECENTLY_SENT_STORENAME, new WrappingStorageUtility(TransportMessageStore.RECENTLY_SENT_STORENAME,new TransportMessageSerializationWrapper(),f));
		ReferenceManager._().addRawReferenceRoot(new HttpRoot());
	}

}
