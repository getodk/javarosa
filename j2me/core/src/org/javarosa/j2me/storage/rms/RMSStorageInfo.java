package org.javarosa.j2me.storage.rms;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * The 'master record' for an RMSStorageUility, containing key information like number of active data stores, etc.
 * 
 * @author Drew Roos
 *
 */
public class RMSStorageInfo implements Externalizable {
	public int numRecords;
	public int numDataStores;
	public int nextRecordID;
	
	public RMSStorageInfo () {
		numRecords = 0;
		numDataStores = 0;
		nextRecordID = 1;
	}
	
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		numRecords = ExtUtil.readInt(in);
		numDataStores = ExtUtil.readInt(in);
		nextRecordID = ExtUtil.readInt(in);
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.writeNumeric(out, numRecords);
		ExtUtil.writeNumeric(out, numDataStores);
		ExtUtil.writeNumeric(out, nextRecordID);
	}
}
