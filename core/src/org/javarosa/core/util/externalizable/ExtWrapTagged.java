/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.core.util.externalizable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class ExtWrapTagged extends ExternalizableWrapper {
	public final static byte[] WRAPPER_TAG = {(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff}; //must be same length as PrototypeFactory.CLASS_HASH_SIZE
	
	public static HashMap<Class, Integer> WRAPPER_CODES;
	
	static {
		WRAPPER_CODES = new HashMap<Class, Integer>();
		WRAPPER_CODES.put(ExtWrapNullable.class, Integer.valueOf(0x00));
		WRAPPER_CODES.put(ExtWrapList.class, Integer.valueOf(0x20));
		WRAPPER_CODES.put(ExtWrapListPoly.class, Integer.valueOf(0x21));
		WRAPPER_CODES.put(ExtWrapMap.class, Integer.valueOf(0x22));
		WRAPPER_CODES.put(ExtWrapMapPoly.class, Integer.valueOf(0x23));
		WRAPPER_CODES.put(ExtWrapIntEncodingUniform.class, Integer.valueOf(0x40));
		WRAPPER_CODES.put(ExtWrapIntEncodingSmall.class, Integer.valueOf(0x41));
	}
	
	/* serialization */
	
	public ExtWrapTagged (Object val) {
		if (val == null) {
			throw new NullPointerException();
		} else if (val instanceof ExtWrapTagged) {
			throw new IllegalArgumentException("Wrapping tagged with tagged is redundant");
		}
		
		this.val = val;
	}
	
	/* deserialization */
	
	public ExtWrapTagged () {

	}
	
	public ExternalizableWrapper clone (Object val) {
		return new ExtWrapTagged(val);
	}
	
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		ExternalizableWrapper type = readTag(in, pf);
		val = ExtUtil.read(in, type, pf);
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		writeTag(out, val);
		ExtUtil.write(out, val);
	}

	public static ExternalizableWrapper readTag (DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		byte[] tag = new byte[PrototypeFactory.CLASS_HASH_SIZE];
		in.read(tag, 0, tag.length);
		
		if (PrototypeFactory.compareHash(tag, WRAPPER_TAG)) {
			int wrapperCode = ExtUtil.readInt(in);
			
			//find wrapper indicated by code
			ExternalizableWrapper type = null;
      for (Object key : WRAPPER_CODES.keySet()) {
				Class t = (Class)key;
				if (WRAPPER_CODES.get(t) == wrapperCode) {
					try {
						type = (ExternalizableWrapper)PrototypeFactory.getInstance(t);
					} catch (CannotCreateObjectException ccoe) {
						throw new CannotCreateObjectException("Serious problem: cannot create built-in ExternalizableWrapper [" + t.getName() + "]");
					}
				}
			}
			if (type == null) {
				throw new DeserializationException("Unrecognized ExternalizableWrapper type [" + wrapperCode + "]");
			}
			
			type.metaReadExternal(in, pf);
			return type;
		} else {
			Class type = pf.getClass(tag);
			if (type == null) {
				throw new DeserializationException("No datatype registered to serialization code " + ExtUtil.printBytes(tag));
			}
			
			return new ExtWrapBase(type);
		}		
	}
	
	public static void writeTag (DataOutputStream out, Object o) throws IOException {
		if (o instanceof ExternalizableWrapper && !(o instanceof ExtWrapBase)) {
			out.write(WRAPPER_TAG, 0, PrototypeFactory.CLASS_HASH_SIZE);
			ExtUtil.writeNumeric(out, WRAPPER_CODES.get(o.getClass()));
			((ExternalizableWrapper)o).metaWriteExternal(out);
		} else {
			Class type = null;
			
			if (o instanceof ExtWrapBase) {
				ExtWrapBase extType = (ExtWrapBase)o;
				if (extType.val != null) {
					o = extType.val;
				} else {
					type = extType.type;
				}
			}
			if (type == null) {
				type = o.getClass();
			}
				
			byte[] tag = PrototypeFactory.getClassHash(type); //cache this?
			out.write(tag, 0, tag.length);
		}
	}

	public void metaReadExternal(DataInputStream in, PrototypeFactory pf) {
		throw new RuntimeException("Tagged wrapper should never be tagged");
	}

	public void metaWriteExternal(DataOutputStream out) {
		throw new RuntimeException("Tagged wrapper should never be tagged");
	}
}
