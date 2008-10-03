package org.javarosa.core.util.externalizable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;


public class ExtWrapBase extends ExternalizableWrapper {
	public Class type;
	
	/* serialization */
	
	public ExtWrapBase (Object val) {
		if (val == null) {
			throw new NullPointerException();
		} else if (val instanceof ExternalizableWrapper) {
			throw new IllegalArgumentException("ExtWrapBase can only contain base types");
		}
			
		this.val = val;
	}
	
	/* deserialization */
	
	public ExtWrapBase (Class type) {
		if (type == null) {
			throw new NullPointerException();
		} else if (ExternalizableWrapper.class.isAssignableFrom(type)) {
			throw new IllegalArgumentException("ExtWrapBase can only contain base types");
		}
			
		this.type = type;
	}
	
	public ExternalizableWrapper clone (Object val) {
		return new ExtWrapBase(val);
	}
	
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		val = ExtUtil.read(in, type, pf);
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.write(out, val);
	}

	public void metaReadExternal (DataInputStream in, PrototypeFactory pf) {
		throw new RuntimeException("Identity wrapper should never be tagged");
	}

	public void metaWriteExternal (DataOutputStream out) {
		throw new RuntimeException("Identity wrapper should never be tagged");
	}
}
