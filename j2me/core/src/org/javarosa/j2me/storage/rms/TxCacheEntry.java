package org.javarosa.j2me.storage.rms;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

public class TxCacheEntry implements Externalizable {
	int id = -1;
	
	int tx_id;
	String rms;
	int rec_id;
	byte[] data;

	public TxCacheEntry () {
		
	}
	
	public TxCacheEntry (RMSTransaction tx, RMSStorageUtility rms, int rec_id, boolean rec_exists) {
		this.tx_id = tx.tx_id;
		this.rms = rms.getName();
		this.rec_id = rec_id;
		
		if (rec_exists) {
			this.data = rms.readBytes(rec_id);
		} else {
			this.data = null;
		}
	}

	public RawRecord toRawRec () {
		if (data == null) {
			throw new IllegalStateException("record must have data");
		}
		
		return new RawRecord(rec_id, data);
	}
	
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		id = ExtUtil.readInt(in);
		tx_id = ExtUtil.readInt(in);
		rms = ExtUtil.readString(in);
		rec_id = ExtUtil.readInt(in);
		data = (byte[])ExtUtil.read(in, new ExtWrapNullable(byte[].class));
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.writeNumeric(out, id);
		ExtUtil.writeNumeric(out, tx_id);
		ExtUtil.writeString(out, rms);
		ExtUtil.writeNumeric(out, rec_id);
		ExtUtil.write(out, new ExtWrapNullable(data));
	}
}
