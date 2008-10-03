/**
 * 
 */
package org.javarosa.model.xform;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.IDataReference;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExternalizableHelperDeprecated;
import org.javarosa.core.util.externalizable.PrototypeFactory;

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
	
	public boolean equals (Object o) {
		if (o instanceof XPathReference) {
			return nodeset.equals(((XPathReference)o).nodeset);
		} else {
			return false;
		}
	}
	
	public int hashCode () {
		return nodeset.hashCode();
	}
	
	public IDataReference clone () {
		return new XPathReference(nodeset);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#readExternal(java.io.DataInputStream)
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		nodeset = ExternalizableHelperDeprecated.readUTF(in);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		ExternalizableHelperDeprecated.writeUTF(out, nodeset);
	}
}
