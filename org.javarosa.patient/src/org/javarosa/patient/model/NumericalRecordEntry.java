package org.javarosa.patient.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

import org.javarosa.core.services.storage.utilities.Externalizable;
import org.javarosa.core.services.storage.utilities.UnavailableExternalizerException;

public class NumericalRecordEntry implements IRecordEntry, Externalizable {
	Date recordDate;
	
	//Sticking with Int for now
	int recordValue;
	
	public NumericalRecordEntry(Date recordDate, int recordValue) {
		this.recordValue = recordValue;
		this.recordDate = recordDate;
	}
	
	public Date getEntryDate() {
		return recordDate;
	}
	
	public int getRecordValue() {
		return recordValue;
	}

	public void readExternal(DataInputStream in) throws IOException,
			InstantiationException, IllegalAccessException,
			UnavailableExternalizerException {
		recordValue = in.readInt();
		
		recordDate = new Date(in.readLong());
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		out.writeInt(recordValue);
		
		out.writeLong(recordDate.getTime());
	}
	
	
}
