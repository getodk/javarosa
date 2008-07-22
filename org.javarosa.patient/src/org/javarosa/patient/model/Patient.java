package org.javarosa.patient.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.utils.ExternalizableHelper;
import org.javarosa.core.services.storage.utilities.Externalizable;
import org.javarosa.core.services.storage.utilities.UnavailableExternalizerException;

public class Patient implements Externalizable {

	int recordId;
	
	String firstName;
	String surname;
	
	
	public String getFullName() {
		return firstName + ", " + surname;
	}
	
	/**
	 * @return the recordId
	 */
	public int getRecordId() {
		return recordId;
	}

	/**
	 * @param recordId the recordId to set
	 */
	public void setRecordId(int recordId) {
		this.recordId = recordId;
	}



	public void readExternal(DataInputStream in) throws IOException,
			InstantiationException, IllegalAccessException,
			UnavailableExternalizerException {
		recordId = in.readInt();
	
		firstName = ExternalizableHelper.readUTF(in);
		surname = ExternalizableHelper.readUTF(in);

	}
	
	public void writeExternal(DataOutputStream out) throws IOException {
		out.writeInt(recordId);
		
		ExternalizableHelper.writeUTF(out, firstName);
		ExternalizableHelper.writeUTF(out, surname);
	}

}
