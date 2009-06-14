package org.javarosa.core.model.test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.IDataReference;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * Dummy reference for testing purposes.
 * 
 * @author Clayton Sims
 *
 */
public class DummyReference implements IDataReference {
	String ref = "";
	
	public DummyReference() {
		
	}
	
	public Object getReference() {
		return ref;
	}

	public void setReference(Object reference) {
		ref = (String)reference;
		
	}

	public void readExternal(DataInputStream in, PrototypeFactory pf)
			throws IOException, DeserializationException {
		ref = in.readUTF();
		
	}

	public void writeExternal(DataOutputStream out)
			throws IOException {
		out.writeUTF(ref);
	}
	
	public boolean equals(Object o) {
		if(!(o instanceof DummyReference)) {
			return false;
		} else {
			return ((DummyReference)o).ref.equals(this.ref);
		}
	}
}
