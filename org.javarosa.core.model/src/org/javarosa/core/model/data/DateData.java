package org.javarosa.core.model.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.javarosa.core.util.externalizable.ExternalizableHelperDeprecated;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * A response to a question requesting a Date Value
 * @author Drew Roos
 *
 */
public class DateData implements IAnswerData {
	Date d;
	
	/**
	 * Empty Constructor, necessary for dynamic construction during deserialization.
	 * Shouldn't be used otherwise.
	 */
	public DateData() {
		
	}
	
	public DateData (Date d) {
		setValue(d);
	}
	
	public void setValue (Object o) {
		//Should not ever be possible to set this to a null value
		if(o == null) {
			throw new NullPointerException("Attempt to set an IAnswerData class to null.");
		}
		d = new Date(((Date)o).getTime());
	}
	
	public Object getValue () {
		return new Date(d.getTime());
	}
	
	public String getDisplayText () {
		//move to util library
		Calendar cd = Calendar.getInstance();
		cd.setTime(d);
		String year = "" + cd.get(Calendar.YEAR);
		String month = "" + (cd.get(Calendar.MONTH)+1);
		String day = "" + cd.get(Calendar.DAY_OF_MONTH);

		if (month.length() < 2)
			month = "0" + month;

		if (day.length() < 2)
			day = "0" + day;

		return day + "/" + month + "/" + year.substring(2,4);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#readExternal(java.io.DataInputStream)
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		this.setValue(ExternalizableHelperDeprecated.readDate(in));
		
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		ExternalizableHelperDeprecated.writeDate(out, this.d);
	}
}
