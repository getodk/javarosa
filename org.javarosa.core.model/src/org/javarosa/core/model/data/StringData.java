package org.javarosa.core.model.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.services.storage.utilities.UnavailableExternalizerException;

public class StringData implements IAnswerData {
	String s;
	
	public StringData (String s) {
		setValue(s);
	}
	
	public void setValue (Object o) {
		s = (String)o;
		if (s == null)
			s = "";
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
		this.setValue(in.readUTF());
		
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		out.writeUTF(this.s);
	}
}
