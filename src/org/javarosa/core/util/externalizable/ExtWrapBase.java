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
