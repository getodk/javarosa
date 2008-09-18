package org.javarosa.core.util.externalizable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.javarosa.core.util.UnavailableExternalizerException;

public class ExtType extends ExternalizableWrapper {
	public Class type;
	
	/* serialization */
	
	public ExtType (Object val) {
		if (val == null) {
			throw new NullPointerException();
		} else if (val instanceof ExternalizableWrapper) {
			throw new IllegalArgumentException("ExtType can only contain base types");
		}
			
		this.val = val;
	}
	
	/* deserialization */
	
	public ExtType (Class type) {
		if (type == null) {
			throw new NullPointerException();
		} else if (ExternalizableWrapper.class.isAssignableFrom(type)) {
			throw new IllegalArgumentException("ExtType can only contain base types");
		}
			
		this.type = type;
	}
	
	public ExternalizableWrapper clone (Object val) {
		return new ExtType(val);
	}
	
	public void readExternal(DataInputStream in, Vector prototypes) throws
		IOException, InstantiationException, IllegalAccessException, UnavailableExternalizerException {
		val = ExtUtil.read(in, type);
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.write(out, val);
	}

	public void metaReadExternal (DataInputStream in, Vector prototypes) {
		throw new RuntimeException("Identity wrapper should never be tagged");
	}

	public void metaWriteExternal (DataOutputStream out) {
		throw new RuntimeException("Identity wrapper should never be tagged");
	}
}
