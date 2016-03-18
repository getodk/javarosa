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
import java.util.ArrayList;
import java.util.List;


//list of objects of single (non-polymorphic) type
public class ExtWrapList extends ExternalizableWrapper {
	public ExternalizableWrapper type;
	private boolean sealed;

	/* serialization */

	public ExtWrapList (List val) {
		this(val, null);
	}

	public ExtWrapList (List val, ExternalizableWrapper type) {
		if (val == null) {
			throw new NullPointerException();
		}

		this.val = val;
		this.type = type;
		this.sealed = false;
	}

	/* deserialization */

	public ExtWrapList () {
		this.sealed = false;
	}

	public ExtWrapList (Class type) {
		this(type, false);
	}

	public ExtWrapList (Class type, boolean sealed) {
		this.type = new ExtWrapBase(type);
		this.sealed = sealed;
	}

	public ExtWrapList (ExternalizableWrapper type) {
		if (type == null) {
			throw new NullPointerException();
		}

		this.type = type;
		this.sealed = false;
	}

	public ExternalizableWrapper clone (Object val) {
		return new ExtWrapList((List)val, type);
	}

	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		if(!sealed) {
			long size = ExtUtil.readNumeric(in);
         List v = new ArrayList((int) size);
			for (int i = 0; i < size; i++) {
				v.add(ExtUtil.read(in, type, pf));
			}
			val = v;
		} else {
			int size = (int)ExtUtil.readNumeric(in);
			Object[] theval = new Object[size];
			for (int i = 0; i < size; i++) {
				theval[i] = ExtUtil.read(in, type, pf);
			}
			val = theval;
		}
	}

	public void writeExternal(DataOutputStream out) throws IOException {
      List v = (List)val;

		ExtUtil.writeNumeric(out, v.size());
		for (int i = 0; i < v.size(); i++) {
			ExtUtil.write(out, type == null ? v.get(i) : type.clone(v.get(i)));
		}
	}

	public void metaReadExternal (DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		type = ExtWrapTagged.readTag(in, pf);
	}

	public void metaWriteExternal (DataOutputStream out) throws IOException {
      List v = (List)val;
		Object tagObj;

		if (type == null) {
			if (v.size() == 0) {
				tagObj = new Object();
			} else {
				tagObj = v.get(0);
			}
		} else {
			tagObj = type;
		}

		ExtWrapTagged.writeTag(out, tagObj);
	}
}
