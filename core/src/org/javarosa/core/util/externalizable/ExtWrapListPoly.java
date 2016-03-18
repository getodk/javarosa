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


//list of objects of multiple types
//if elements are compound types (i.e., need wrappers), they must be pre-wrapped before invoking this wrapper, because... come on now.
public class ExtWrapListPoly extends ExternalizableWrapper {
	/* serializaiton */

	public ExtWrapListPoly (List val) {
		if (val == null) {
			throw new NullPointerException();
		}

		this.val = val;
	}

	/* deserialization */

	public ExtWrapListPoly () {

	}

	public ExternalizableWrapper clone (Object val) {
		return new ExtWrapListPoly((List)val);
	}

	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {

		long size = ExtUtil.readNumeric(in);
      List v = new ArrayList((int) size);
		for (int i = 0; i < size; i++) {
			v.add(ExtUtil.read(in, new ExtWrapTagged(), pf));
		}

		val = v;
	}

	public void writeExternal(DataOutputStream out) throws IOException {
      List v = (List)val;

		ExtUtil.writeNumeric(out, v.size());
		for (int i = 0; i < v.size(); i++) {
			ExtUtil.write(out, new ExtWrapTagged(v.get(i)));
		}
	}

	public void metaReadExternal (DataInputStream in, PrototypeFactory pf) {
		//do nothing
	}

	public void metaWriteExternal (DataOutputStream out) {
		//do nothing
	}
}
