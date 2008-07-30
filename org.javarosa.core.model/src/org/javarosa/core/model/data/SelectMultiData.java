package org.javarosa.core.model.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.javarosa.core.model.utils.ExternalizableHelper;
import org.javarosa.core.util.UnavailableExternalizerException;

public class SelectMultiData implements IAnswerData {
	Vector vs; //vector of Selection
	
	/**
	 * Empty Constructor, necessary for dynamic construction during deserialization.
	 * Shouldn't be used otherwise.
	 */
	public SelectMultiData() {
		
	}
	
	public SelectMultiData (Vector vs) {
		setValue(vs);
	}
	
	public void setValue (Object o) {
		vs = (Vector)o;
		
		//validate type
		for (int i = 0; i < vs.size(); i++) {
			Selection s = (Selection)vs.elementAt(i);
		}
	}
	
	public Object getValue () {
		return vs;
	}
	
	public String getDisplayText () {
		String str = "";
		
		for (int i = 0; i < vs.size(); i++) {
			Selection s = (Selection)vs.elementAt(i);
			str += s.getText();
			if (i < vs.size() - 1)
				str += ", ";
		}		
		
		return str;
	}
	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#readExternal(java.io.DataInputStream)
	 */
	public void readExternal(DataInputStream in) throws IOException,
			InstantiationException, IllegalAccessException,
			UnavailableExternalizerException {
		vs = ExternalizableHelper.readExternal(in, Selection.class);
		
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		ExternalizableHelper.writeExternal(vs, out);
	}
}
