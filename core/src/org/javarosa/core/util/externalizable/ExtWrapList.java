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
import java.util.Vector;


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
		this.type = new ExtWrapBase(type);
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
	
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		Vector v = new Vector(0);

		long size = ExtUtil.readNumeric(in);
		for (int i = 0; i < size; i++) {
			v.addElement(ExtUtil.read(in, type, pf));
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

	public void metaReadExternal (DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		type = ExtWrapTagged.readTag(in, pf);
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
