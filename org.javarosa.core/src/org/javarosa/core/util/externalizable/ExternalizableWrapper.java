package org.javarosa.core.util.externalizable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.javarosa.core.util.Externalizable;
import org.javarosa.core.util.UnavailableExternalizerException;

/**
 * issues
 * 
 * * not passing prototypes around everywhere. might not make it deep down in the tree where needed?
 * * conflicting constructors... null(listwrapper())... confuses listwrapper as type, even though it contains val
 */

public abstract class ExternalizableWrapper implements Externalizable {
	/* core data that is being wrapped; will be null when shell wrapper is created for deserialization */
	public Object val;
	
	/* create a copy of a wrapper, but with new val (but all the same type annotations */
	public abstract ExternalizableWrapper clone (Object val);
	
	public void readExternal(DataInputStream in) throws
		IOException, InstantiationException, IllegalAccessException, UnavailableExternalizerException {
		readExternal(in, ExtWrapTagged.initPrototypes(null));
	}
	
	public abstract void readExternal(DataInputStream in, Vector prototypes) throws
		IOException, InstantiationException, IllegalAccessException, UnavailableExternalizerException;
	
	/* serialize the state of the externalizable wrapper (type information only, not value) */
	public abstract void metaWriteExternal (DataOutputStream out) throws IOException;

	/* deserialize the state of the externalizable wrapper */
	public abstract void metaReadExternal (DataInputStream in, Vector prototypes) throws
		IOException, UnavailableExternalizerException, IllegalAccessException, InstantiationException;
	
	/**
	 *  constructor guidelines: each child of this class should follow these rules with its constructors
	 *  
	 *  1) every constructor that sets 'val' should have a matching constructor for deserialization that
	 *     leaves 'val' null
	 *  2) every constructor that accepts an ExternalizableWrapper should also have a convenience constructor
	 *     that accepts a Class, and wraps the Class in an ExtType (the identity wrapper)
	 *  3) there must exist a null constructor for meta-deserialization (for applicable wrappers)
	 */
	
	public Object baseValue () {
		Object baseVal = val;
		while (baseVal instanceof ExternalizableWrapper) {
			baseVal = ((ExternalizableWrapper)baseVal).val;
		}
		return baseVal;
	}
	
	public boolean isEmpty () {
		return baseValue() == null;
	}
}
