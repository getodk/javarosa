/*
 * Copyright (C) 2009 JavaRosa-Core Project
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
import java.util.Enumeration;
import java.util.Hashtable;

import org.javarosa.core.util.OrderedHashtable;

//map of objects where key and data are all of single (non-polymorphic) type (key and value can be of separate types)
public class ExtWrapMap extends ExternalizableWrapper {
	public ExternalizableWrapper keyType;
	public ExternalizableWrapper dataType;
	public boolean ordered;
	
	/* serialization */
	
	public ExtWrapMap (Hashtable val) {
		this(val, null, null);
	}
	
	public ExtWrapMap (Hashtable val, ExternalizableWrapper dataType) {
		this(val, null, dataType);
	}
		
	public ExtWrapMap (Hashtable val, ExternalizableWrapper keyType, ExternalizableWrapper dataType) {
		if (val == null) {
			throw new NullPointerException();
		}
		
		this.val = val;
		this.keyType = keyType;
		this.dataType = dataType;
		this.ordered = (val instanceof OrderedHashtable);
	}

	/* deserialization */
	
	public ExtWrapMap () {
		
	}

	public ExtWrapMap (Class keyType, Class dataType) {
		this(keyType, dataType, false);
	}

	public ExtWrapMap (Class keyType, ExternalizableWrapper dataType) {
		this(keyType, dataType, false);
	}
	
	public ExtWrapMap (ExternalizableWrapper keyType, ExternalizableWrapper dataType) {
		this(keyType, dataType, false);
	}
	
	public ExtWrapMap (Class keyType, Class dataType, boolean ordered) {
		this(new ExtWrapBase(keyType), new ExtWrapBase(dataType), ordered);
	}

	public ExtWrapMap (Class keyType, ExternalizableWrapper dataType, boolean ordered) {
		this(new ExtWrapBase(keyType), dataType, ordered);
	}
	
	public ExtWrapMap (ExternalizableWrapper keyType, ExternalizableWrapper dataType, boolean ordered) {
		if (keyType == null || dataType == null) {
			throw new NullPointerException();
		}
		
		this.keyType = keyType;
		this.dataType = dataType;
		this.ordered = ordered;
	}
	
	public ExternalizableWrapper clone (Object val) {
		return new ExtWrapMap((Hashtable)val, keyType, dataType);
	}
	
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		Hashtable h = ordered ? new OrderedHashtable() : new Hashtable();

		long size = ExtUtil.readNumeric(in);
		for (int i = 0; i < size; i++) {
			Object key = ExtUtil.read(in, keyType, pf);
			Object elem = ExtUtil.read(in, dataType, pf);
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
			ExtUtil.write(out, dataType == null ? elem : dataType.clone(elem));			
		}
	}

	public void metaReadExternal (DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		ordered = ExtUtil.readBool(in);
		keyType = ExtWrapTagged.readTag(in, pf);
		dataType = ExtWrapTagged.readTag(in, pf);
	}

	public void metaWriteExternal (DataOutputStream out) throws IOException {
		Hashtable h = (Hashtable)val;
		Object keyTagObj, elemTagObj;
		
		keyTagObj = (keyType == null ? (h.size() == 0 ? new Object() : h.keys().nextElement()) : keyType);
		elemTagObj = (dataType == null ? (h.size() == 0 ? new Object() : h.elements().nextElement()) : dataType);
		
		ExtUtil.writeBool(out, ordered);
		ExtWrapTagged.writeTag(out, keyTagObj);
		ExtWrapTagged.writeTag(out, elemTagObj);
	}
}
