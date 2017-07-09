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

import org.javarosa.core.util.Map;
import org.javarosa.core.util.OrderedMap;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;

//map of objects where key and data are all of single (non-polymorphic) type (key and value can be of separate types)
public class ExtWrapMap extends ExternalizableWrapper {

	public static final int TYPE_NORMAL = 0;
	public static final int TYPE_ORDERED = 1;
	public static final int TYPE_SLOW_COMPACT = 2;
	public static final int TYPE_SLOW_READ_ONLY = 4;

	public ExternalizableWrapper keyType;
	public ExternalizableWrapper dataType;
	public int type;

	/* serialization */

	public ExtWrapMap (HashMap val) {
		this(val, null, null);
	}

	public ExtWrapMap (HashMap val, ExternalizableWrapper dataType) {
		this(val, null, dataType);
	}

	public ExtWrapMap (java.util.Map val, ExternalizableWrapper keyType, ExternalizableWrapper dataType) {
		if (val == null) {
			throw new NullPointerException();
		}

		this.val = val;
		this.keyType = keyType;
		this.dataType = dataType;
		if(val instanceof Map) {
			//TODO: check for sealed
			type = TYPE_SLOW_READ_ONLY;
		} else if (val instanceof OrderedMap) {
			type = TYPE_ORDERED;
		} else {
			type = TYPE_NORMAL;
		}
	}

	/* deserialization */

	public ExtWrapMap () {

	}

	public ExtWrapMap (Class keyType, Class dataType) {
		this(keyType, dataType, TYPE_NORMAL);
	}

	public ExtWrapMap (Class keyType, ExternalizableWrapper dataType) {
		this(keyType, dataType, TYPE_NORMAL);
	}

	public ExtWrapMap (ExternalizableWrapper keyType, ExternalizableWrapper dataType) {
		this(keyType, dataType, TYPE_NORMAL);
	}

	public ExtWrapMap (Class keyType, Class dataType, int type) {
		this(new ExtWrapBase(keyType), new ExtWrapBase(dataType), type);
	}

	public ExtWrapMap (Class keyType, ExternalizableWrapper dataType, int type) {
		this(new ExtWrapBase(keyType), dataType, type);
	}

	public ExtWrapMap (ExternalizableWrapper keyType, ExternalizableWrapper dataType, int type) {
		if (keyType == null || dataType == null) {
			throw new NullPointerException();
		}

		this.keyType = keyType;
		this.dataType = dataType;
		this.type = type;
	}

	public ExternalizableWrapper clone (Object val) {
		return new ExtWrapMap((HashMap)val, keyType, dataType);
	}

	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		if(type != TYPE_SLOW_READ_ONLY) {
			java.util.Map h;
			long size = ExtUtil.readNumeric(in);
			switch(type) {
			case(TYPE_NORMAL):
				h = new HashMap((int)size);
				break;
			case(TYPE_ORDERED):
				h = new OrderedMap();
				break;
			case(TYPE_SLOW_COMPACT):
				h = new Map((int)size);
				break;
			default:
				h = new HashMap((int)size);
			}

			for (int i = 0; i < size; i++) {
				Object key = ExtUtil.read(in, keyType, pf);
				Object elem = ExtUtil.read(in, dataType, pf);
				h.put(key, elem);
			}

			val = h;
		} else {
			int size = ExtUtil.readInt(in);
			Object[] k = new Object[size];
			Object[] v = new Object[size];
			for (int i = 0; i < size; i++) {
				k[i] = ExtUtil.read(in, keyType, pf);
				v[i] = ExtUtil.read(in, dataType, pf);
			}
			val = new Map(k, v);
		}
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		HashMap h = (HashMap)val;

		ExtUtil.writeNumeric(out, h.size());
    for (Object key : h.keySet()) {
			Object elem = h.get(key);

			ExtUtil.write(out, keyType == null ? key : keyType.clone(key));
			ExtUtil.write(out, dataType == null ? elem : dataType.clone(elem));
		}
	}

	public void metaReadExternal (DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		type = ExtUtil.readInt(in);
		keyType = ExtWrapTagged.readTag(in, pf);
		dataType = ExtWrapTagged.readTag(in, pf);
	}

	public void metaWriteExternal (DataOutputStream out) throws IOException {
		HashMap h = (HashMap)val;
		Object keyTagObj, elemTagObj;

		keyTagObj = (keyType == null ? (h.size() == 0 ? new Object() : h.keySet().iterator().next()) : keyType);
		elemTagObj = (dataType == null ? (h.size() == 0 ? new Object() : h.values().iterator().next()) : dataType);

		ExtUtil.writeNumeric(out, type);
		ExtWrapTagged.writeTag(out, keyTagObj);
		ExtWrapTagged.writeTag(out, elemTagObj);
	}
}
