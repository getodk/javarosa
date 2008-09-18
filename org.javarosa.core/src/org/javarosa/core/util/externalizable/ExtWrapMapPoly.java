package org.javarosa.core.util.externalizable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.javarosa.core.util.OrderedHashtable;
import org.javarosa.core.util.UnavailableExternalizerException;

//map of objects where elements are multiple types, keys are still assumed to be of a single (non-polymorphic) type
//if elements are compound types (i.e., need wrappers), they must be pre-wrapped before invoking this wrapper, because... come on now.
public class ExtWrapMapPoly extends ExternalizableWrapper {
	public ExternalizableWrapper keyType;
	public boolean ordered;
	
	/* serialization */
	
	public ExtWrapMapPoly (Hashtable val) {
		this(val, null);
	}
	
	public ExtWrapMapPoly (Hashtable val, ExternalizableWrapper keyType) {
		if (val == null) {
			throw new NullPointerException();
		}
		
		this.val = val;
		this.keyType = keyType;
	}

	/* deserialization */
	
	public ExtWrapMapPoly () {
		
	}

	public ExtWrapMapPoly (Class keyType) {
		this(keyType, false);
	}
	
	public ExtWrapMapPoly (ExternalizableWrapper keyType) {
		this(keyType, false);
	}
	
	public ExtWrapMapPoly (Class keyType, boolean ordered) {
		this(new ExtType(keyType), ordered);
	}
	
	public ExtWrapMapPoly (ExternalizableWrapper keyType, boolean ordered) {
		if (keyType == null) {
			throw new NullPointerException();
		}
		
		this.keyType = keyType;
		this.ordered = ordered;
	}
	
	public ExternalizableWrapper clone (Object val) {
		return new ExtWrapMapPoly((Hashtable)val, keyType);
	}
	
	public void readExternal(DataInputStream in, Vector prototypes) throws 
		IOException, UnavailableExternalizerException, IllegalAccessException, InstantiationException {
		Hashtable h = ordered ? new OrderedHashtable() : new Hashtable();

		long size = ExtUtil.readNumeric(in);
		for (int i = 0; i < size; i++) {
			Object key = ExtUtil.read(in, keyType, prototypes);
			Object elem = ExtUtil.read(in, new ExtWrapTagged(), prototypes);
			h.put(key, elem);
		}
		
		val = h;
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		Hashtable h = (Hashtable)val;

		ExtUtil.writeNumeric(out, h.size());
		for (Enumeration e = h.keys(); e.hasMoreElements(); ) {
			Object key = e.nextElement();
			Object elem = h.get(key);
			
			ExtUtil.write(out, keyType == null ? key : keyType.clone(key));
			ExtUtil.write(out, new ExtWrapTagged(elem));			
		}		
	}

	public void metaReadExternal (DataInputStream in, Vector prototypes) throws
		IOException, UnavailableExternalizerException, IllegalAccessException, InstantiationException {
		keyType = ExtWrapTagged.readTag(in, prototypes);
	}

	public void metaWriteExternal (DataOutputStream out) throws IOException {
		Hashtable h = (Hashtable)val;
		Object keyTagObj;
		
		keyTagObj = (keyType == null ? (h.size() == 0 ? new Object() : h.keys().nextElement()) : keyType);		
		ExtWrapTagged.writeTag(out, keyTagObj);
	}
}
