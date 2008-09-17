package org.javarosa.core.util.externalizable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.javarosa.core.util.UnavailableExternalizerException;

//list of objects of single (non-polymorphic) type
public class ExtWrapList extends ExternalizableWrapper {
	public ExternalizableWrapper type;
	
	/* serialization */
	
	public ExtWrapList (Vector val) {
		this(val, null);
	}
	
	public ExtWrapList (Vector val, ExternalizableWrapper type) {
		if (val == null) {
			throw new NullPointerException();
		}
		
		this.val = val;
		this.type = type;
	}

	/* deserialization */
	
	public ExtWrapList () {
		
	}
	
	public ExtWrapList (Class type) {
		this.type = new ExtType(type);
	}

	public ExtWrapList (ExternalizableWrapper type) {
		if (type == null) {
			throw new NullPointerException();
		}
		
		this.type = type;
	}
	
	public ExternalizableWrapper clone (Object val) {
		return new ExtWrapList((Vector)val, type);
	}
	
	public void readExternal(DataInputStream in) throws 
		IOException, UnavailableExternalizerException, IllegalAccessException, InstantiationException {
		Vector v = new Vector();

		long size = ExtUtil.readNumeric(in);
		for (int i = 0; i < size; i++) {
			v.addElement(ExtUtil.read(in, type));
		}
		
		val = v;
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		Vector v = (Vector)val;

		ExtUtil.writeNumeric(out, v.size());
		for (int i = 0; i < v.size(); i++) {
			ExtUtil.write(out, type == null ? v.elementAt(i) : type.clone(v.elementAt(i)));
		}
	}

	public void metaReadExternal (DataInputStream in, Vector prototypes) throws
		IOException, UnavailableExternalizerException, IllegalAccessException, InstantiationException {
		type = ExtWrapTagged.readTag(in, prototypes);
	}

	public void metaWriteExternal (DataOutputStream out) throws IOException {
		Vector v = (Vector)val;
		Object tagObj;
		
		if (type == null) {
			if (v.size() == 0) {
				tagObj = new Object();
			} else {
				tagObj = v.elementAt(0);
			}
		} else {
			tagObj = type;
		}
		
		ExtWrapTagged.writeTag(out, tagObj);
	}
}
