package org.javarosa.patient.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

import org.javarosa.core.model.utils.ExternalizableHelper;
import org.javarosa.core.util.Externalizable;
import org.javarosa.core.util.UnavailableExternalizerException;

public class DateValueTuple implements Externalizable {
	public Date date;
	public int value;
	
	public DateValueTuple(){
		
	}
	
	public DateValueTuple(Date date, int value) {
		this.date = date;
		this.value = value;
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#readExternal(java.io.DataInputStream)
	 */
	public void readExternal(DataInputStream in) throws IOException,
			InstantiationException, IllegalAccessException,
			UnavailableExternalizerException {
		date = ExternalizableHelper.readDate(in);
		value = in.readInt();
		
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	 public void writeExternal(DataOutputStream out) throws IOException {
		 ExternalizableHelper.writeDate(out, date);
		 out.writeInt(value);
	}
}
