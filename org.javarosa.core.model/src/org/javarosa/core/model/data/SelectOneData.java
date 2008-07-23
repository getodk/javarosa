package org.javarosa.core.model.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.services.storage.utilities.UnavailableExternalizerException;

public class SelectOneData implements IAnswerData {
	Selection s;
	
	public SelectOneData (Selection s) {
		setValue(s);
	}
	
	public void setValue (Object o) {
		s = (Selection)o;
	}
	
	public Object getValue () {
		return s;
	}
	
	public String getDisplayText () {
		return s.getText();
	}
	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#readExternal(java.io.DataInputStream)
	 */
	public void readExternal(DataInputStream in) throws IOException,
			InstantiationException, IllegalAccessException,
			UnavailableExternalizerException {
		s = new Selection();
		s.readExternal(in);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		s.writeExternal(out);
	}
}
