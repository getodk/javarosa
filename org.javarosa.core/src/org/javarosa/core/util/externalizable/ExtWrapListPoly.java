package org.javarosa.core.util.externalizable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.javarosa.core.util.UnavailableExternalizerException;

//list of objects of multiple types
//if elements are compound types (i.e., need wrappers), they must be pre-wrapped before invoking this wrapper, because... come on now.
public class ExtWrapListPoly extends ExternalizableWrapper {
	/* serializaiton */
	
	public ExtWrapListPoly (Vector val) {
		if (val == null) {
			throw new NullPointerException();
		}
		
		this.val = val;
	}	
	
	/* deserialization */
	
	public ExtWrapListPoly () {

	}

	public ExternalizableWrapper clone (Object val) {
		return new ExtWrapListPoly((Vector)val);
	}	
	
	public void readExternal(DataInputStream in) throws 
		IOException, UnavailableExternalizerException, IllegalAccessException, InstantiationException {
		Vector v = new Vector();
		
		long size = ExtUtil.readNumeric(in);
		for (int i = 0; i < size; i++) {
			v.addElement(ExtUtil.read(in, new ExtWrapTagged(null, null))); //how to get prototypes here?
		}
		
		val = v;
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		Vector v = (Vector)val;
		
		ExtUtil.writeNumeric(out, v.size());
		for (int i = 0; i < v.size(); i++) {
			ExtUtil.write(out, new ExtWrapTagged(v.elementAt(i)));
		}
	}
	
	public void metaReadExternal (DataInputStream in, Vector prototypes) throws
		IOException, UnavailableExternalizerException, IllegalAccessException, InstantiationException {
		//do nothing
	}

	public void metaWriteExternal (DataOutputStream out) throws IOException {
		//do nothing
	}
}
