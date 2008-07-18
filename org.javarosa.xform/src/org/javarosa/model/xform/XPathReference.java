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
	private String nodeset;
	
	public XPathReference () {
		this(null);
	}
	
	public XPathReference (String nodeset) {
		setReference(nodeset);
	}
	
	public Object getReference () {
		return nodeset;
	}
	
	public void setReference (Object o) {
		nodeset = (String)o;
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
			return extReference.nodeset.equals(nodeset);
		}
	}
	
	public IDataReference clone () {
		return new XPathReference(nodeset);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#readExternal(java.io.DataInputStream)
	 */
	public void readExternal(DataInputStream in) throws IOException,
			InstantiationException, IllegalAccessException {
		nodeset = ExternalizableHelper.readUTF(in);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		ExternalizableHelper.writeUTF(out, nodeset);
	}
}
