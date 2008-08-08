package org.javarosa.core.model.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.utils.ExternalizableHelper;
import org.javarosa.core.util.UnavailableExternalizerException;

public class StringData implements IAnswerData {
	String s;

	/**
	 * Empty Constructor, necessary for dynamic construction during deserialization.
	 * Shouldn't be used otherwise.
	 */
	public StringData() {
		
	}
	
	public StringData (String s) {
		setValue(s);
	}

	//string should not be null or empty; the entire StringData reference should be null in this case
	public void setValue (Object o) {
		//#if debug.output == verbose
		if(o == null || ((String)o).length() == 0) {
			System.out.println("StringData is being set to a null value. Should not be allowed.");
		}
		//#endif
		s = (String)o;
	}
	
	public Object getValue () {
		return s;
	}
	
	public String getDisplayText () {
		return s;
	}
	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#readExternal(java.io.DataInputStream)
	 */
	public void readExternal(DataInputStream in) throws IOException,
			InstantiationException, IllegalAccessException,
			UnavailableExternalizerException {
		this.setValue(ExternalizableHelper.readUTF(in));		
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		ExternalizableHelper.writeUTF(out, this.s);
	}
}
