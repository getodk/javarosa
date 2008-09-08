package org.javarosa.core.model.data;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.model.utils.ExternalizableHelper;
import org.javarosa.core.util.UnavailableExternalizerException;

public class TimeData implements IAnswerData {
	Date d;
	
	/**
	 * Empty Constructor, necessary for dynamic construction during deserialization.
	 * Shouldn't be used otherwise.
	 */
	public TimeData() {
	}
	
	public TimeData (Date d) {
		setValue(d);
	}
	
	public void setValue (Object o) {
		if(o == null) {
			throw new NullPointerException("Attempt to set an IAnswerData class to null.");
		}
		d = new Date(((Date)o).getTime());
		
	}
	
	public Object getValue () {
		return new Date(d.getTime());
	}
	
	public String getDisplayText () {
		
		return DateUtils.get24HourTimeFromDate(d);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#readExternal(java.io.DataInputStream)
	 */
	public void readExternal(DataInputStream in) throws IOException,
			InstantiationException, IllegalAccessException,
			UnavailableExternalizerException {
		this.setValue(ExternalizableHelper.readDate(in));
		
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		System.out.println("written out timedata");
		ExternalizableHelper.writeDate(out, this.d);
	}
}
