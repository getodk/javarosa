/**
 * 
 */
package org.javarosa.j2me.log;

import java.io.IOException;
import java.util.Date;
import java.util.Vector;

import org.javarosa.core.api.IIncidentLogger;
import org.javarosa.core.log.ILogSerializer;
import org.javarosa.core.log.IncidentLog;
import org.javarosa.core.services.storage.utilities.IRecordStoreEnumeration;
import org.javarosa.core.services.storage.utilities.RMSUtility;
import org.javarosa.core.services.storage.utilities.RecordStorageException;
import org.javarosa.core.util.externalizable.DeserializationException;

/**
 * @author Clayton Sims
 * @date Apr 10, 2009 
 *
 */
public class J2MEIncidentLogger extends RMSUtility implements IIncidentLogger {
	
	// Clayton Sims - Apr 10, 2009 : 
	//NOTE: This class is actually a logger and an RMS Utility. It is possible that it would be
	//more useful in the future to handle those activities with two different classes.
	
	public static String getUtilityName() {
		return "RMS_INCIDENT_LOGGER";
	}

	public J2MEIncidentLogger() {
		super(getUtilityName(), RMSUtility.RMS_TYPE_STANDARD);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IIncidentLogger#clearLogs()
	 */
	public void clearLogs() {
		this.tempEmpty();
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IIncidentLogger#logIncident(java.lang.String, java.lang.String, java.util.Date)
	 */
	public void logIncident(String type, String message, Date logDate) {
		IncidentLog log = new IncidentLog(type, message, logDate);
		this.writeToRMS(log, null);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IIncidentLogger#serializeLogs()
	 */
	public byte[] serializeLogs(ILogSerializer serializer) {
		Vector logs = new Vector();
		IRecordStoreEnumeration e = this.enumerateRecords();
		while(e.hasNextElement()) {
			IncidentLog log = new IncidentLog();
	    	try {
				super.retrieveFromRMS(e.nextRecordId(), log);
				logs.addElement(log);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (DeserializationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (RecordStorageException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		IncidentLog[] collection = new IncidentLog[logs.size()];
		logs.copyInto(collection);
		return serializer.serializeLogs(collection);
	}

}
