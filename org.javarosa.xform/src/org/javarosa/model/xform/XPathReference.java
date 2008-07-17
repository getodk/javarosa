/**
 * 
 */
package org.javarosa.model.xform;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.utils.ExternalizableHelper;

/**
 * 
 */
public class XPathReference implements IDataReference {
	private String xpath;
	
	public XPathReference () {
		this(null);
	}
	
	public XPathReference (String xpath) {
		setReference(xpath);
	}
	
	public Object getReference () {
		return xpath;
	}
	
	public void setReference (Object o) {
		xpath = (String)o;
	}
	public boolean referenceMatches(IDataReference reference) {
		if(reference.getClass() != XPathReference.class) {
			return getReference().equals(reference);
		}
		else {
			//Put xpath specific reference matching here;
			XPathReference extReference = (XPathReference)reference;
			//This does the same thing as above for now, eventually
			//we should fill in * references and stuff
			return extReference.xpath.equals(xpath);
		}
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#readExternal(java.io.DataInputStream)
	 */
	public void readExternal(DataInputStream in) throws IOException,
			InstantiationException, IllegalAccessException {
		xpath = ExternalizableHelper.readUTF(in);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		ExternalizableHelper.writeUTF(out, xpath);
	}
	
	
}
