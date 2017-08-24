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


public class ExtWrapNullable extends ExternalizableWrapper {
	public ExternalizableWrapper type;
	
	/* serialization */

	public ExtWrapNullable (Object val) {
		this.val = val;
	}

	/* deserialization */
	
	public ExtWrapNullable () {
		
	}
	
	public ExtWrapNullable (Class type) {
		this.type = new ExtWrapBase(type);
	}

	/* serialization or deserialization, depending on context */
	
	public ExtWrapNullable (ExternalizableWrapper type) {
		if (type instanceof ExtWrapNullable) {
			throw new IllegalArgumentException("Wrapping nullable with nullable is redundant");
		} else if (type != null && type.isEmpty()) {
			this.type = type;
		} else {
			this.val = type;
		}
	}
	
	public ExternalizableWrapper clone (Object val) {
		return new ExtWrapNullable(val);
	}
	
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		if (in.readBoolean()) {
			val = ExtUtil.read(in, type, pf);
		} else {
			val = null;
		}
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		if (val != null) {
			out.writeBoolean(true);
			ExtUtil.write(out, val);
		} else {
			out.writeBoolean(false);
		}
	}

	public void metaReadExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		type = ExtWrapTagged.readTag(in, pf);
	}

	public void metaWriteExternal(DataOutputStream out) throws IOException {
		ExtWrapTagged.writeTag(out, val == null ? new Object() : val);
	}
}
