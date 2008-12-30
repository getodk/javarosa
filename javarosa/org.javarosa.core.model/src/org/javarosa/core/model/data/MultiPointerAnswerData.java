package org.javarosa.core.model.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.data.IDataPointer;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * An answer data storing multiple pointers
 * @author Cory Zue
 *
 */
public class MultiPointerAnswerData implements IAnswerData {

	private IDataPointer[] data;
	
	/**
	 * NOTE: Only for serialization/deserialization
	 */
	public MultiPointerAnswerData() {
		//Only for serialization/deserialization
	}
	
	public MultiPointerAnswerData (IDataPointer[] values) {
		data = values;
	}
	
	public String getDisplayText() {
		String toReturn = "";
		for (int i=0; i < data.length; i++) {
			if (i != 0) { 
				toReturn += ", ";
			}
			toReturn += data[i].getDisplayText();
		}
		return toReturn;
	}

	public Object getValue() {
		return data;
	}

	public void setValue(Object o) {
		if(o == null) {
			throw new NullPointerException("Attempt to set an IAnswerData class to null.");
		}
		data = (IDataPointer[]) o;
	}

	public IAnswerData clone () {
		return null; //not cloneable
	}
	
	public void readExternal(DataInputStream in, PrototypeFactory pf)
			throws IOException, DeserializationException {
		int length = in.readInt();
		data = new IDataPointer[length];
		for(int i = 0; i < data.length; ++i) {
			data[i] = (IDataPointer)ExtUtil.read(in, new ExtWrapTagged());
		}
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		out.writeInt(data.length);
		for(int i = 0; i < data.length ; ++i ) {
			ExtUtil.write(out, new ExtWrapTagged(data[i]));
		}
	}

}
