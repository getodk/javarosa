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
import org.javarosa.services.transport.impl.simplehttp.SimpleHttpTransportMessage;

/**
 * @author ctsims
 *
 */
public class TransportManagerModule implements IModule {

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IModule#registerModule()
	 */
	public void registerModule() {
		
		//Note: Do not remove fully qualified names here, otherwise the imports mess up the polish preprocessing 
		
		//#if polish.api.wmapi
		String[] prototypes = new String[] { SimpleHttpTransportMessage.class.getName(), org.javarosa.services.transport.impl.sms.SMSTransportMessage.class.getName(), org.javarosa.services.transport.impl.binarysms.BinarySMSTransportMessage.class.getName(), TransportMessageSerializationWrapper.class.getName()};
		//#else
		//# String[] prototypes = new String[] { SimpleHttpTransportMessage.class.getName(), TransportMessageSerializationWrapper.class.getName()};
		//#endif
		
		PrototypeManager.registerPrototypes(prototypes);	
		
		StorageManager.registerWrappedStorage(TransportMessageStore.Q_STORENAME, TransportMessageStore.Q_STORENAME, new TransportMessageSerializationWrapper());
		StorageManager.registerWrappedStorage(TransportMessageStore.RECENTLY_SENT_STORENAME, TransportMessageStore.RECENTLY_SENT_STORENAME, new TransportMessageSerializationWrapper());
		ReferenceManager._().addReferenceFactory(new HttpRoot());
		TransportService.init();
	}

}
