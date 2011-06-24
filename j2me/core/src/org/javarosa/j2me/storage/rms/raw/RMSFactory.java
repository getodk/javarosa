/**
 * 
 */
package org.javarosa.j2me.storage.rms.raw;

import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotFoundException;

/**
 * @author ctsims
 *
 */
public class RMSFactory {
	public RMS getIndexRMS(String name, boolean create) throws RecordStoreNotFoundException, RecordStoreException {
		return new RMS(name, create);
	}
	
	public RMS getDataRMS(String name, boolean create) throws RecordStoreNotFoundException, RecordStoreException{
		return new RMS(name, create);
	}
}
