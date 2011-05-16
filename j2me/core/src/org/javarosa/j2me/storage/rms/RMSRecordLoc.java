package org.javarosa.j2me.storage.rms;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * A tuple that represents a record's exact location within an RMSStorageUtility, consisting of (RMS #, record ID within that RMS)
 * 
 * @author Drew Roos
 *
 */
public class RMSRecordLoc implements Externalizable {
	public int rmsID;
	public int recID;
	
	public RMSRecordLoc () { }
	
	public RMSRecordLoc (int rmsID, int recID) {
		this.rmsID = rmsID;
		this.recID = recID;
	}

	public boolean equals (Object o) {
		if (o instanceof RMSRecordLoc) {
			RMSRecordLoc r = (RMSRecordLoc)o;
			return (this == o ? true : this.rmsID == r.rmsID && this.recID == r.recID);
		} else {
			return false;
		}
	}
	
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		rmsID = ExtUtil.readInt(in);
		recID = ExtUtil.readInt(in);
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.writeNumeric(out, rmsID);
		ExtUtil.writeNumeric(out, recID);
	}
}