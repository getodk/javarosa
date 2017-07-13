/**
 *
 */
package org.javarosa.core.model.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * Uncast data values are those which are not assigned a particular
 * data type. This is relevant when data is read before a datatype is
 * available, or when it must be pulled from external instances.
 *
 * In general, Uncast data should be used when a value is available
 * in string form, and no adequate assumption can be made about the type
 * of data being represented. This is preferable to making the assumption
 * that data is a StringData object, since that will cause issues when
 * select choices or other typed values are expected.
 *
 * @author ctsims
 *
 */
public class UncastData implements IAnswerData {
	String value;

	public UncastData() {

	}

	public UncastData(String value) {
		if(value == null) {
			throw new NullPointerException("Attempt to set Uncast Data value to null! IAnswerData objects should never have null values");
		}
		this.value = value;
	}

	public IAnswerData clone() {
		return new UncastData(value);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.data.IAnswerData#getDisplayText()
	 */
	public String getDisplayText() {
		return value;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.data.IAnswerData#getValue()
	 */
	public Object getValue() {
		return value;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.data.IAnswerData#setValue(java.lang.Object)
	 */
	public void setValue(Object o) {
		value = (String)o;
	}

	/**
	 * @return The string representation of this data. This value should be
	 * castable into its appropriate data type.
	 */
	public String getString() {
		return value;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.util.externalizable.Externalizable#readExternal(java.io.DataInputStream, org.javarosa.core.util.externalizable.PrototypeFactory)
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf)
			throws IOException, DeserializationException {
		value = ExtUtil.readString(in);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.util.externalizable.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.writeString(out, value);
	}

	public UncastData uncast() {
		return this;
	}

	public UncastData cast(UncastData data) {
		return new UncastData(data.value);
	}
}
