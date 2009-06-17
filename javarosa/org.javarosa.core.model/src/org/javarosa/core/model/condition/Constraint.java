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

package org.javarosa.core.model.condition;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

public class Constraint implements Externalizable {
	public IConditionExpr constraint;
	public String constraintMsg;
	
	public Constraint () { }
	
	public Constraint (IConditionExpr constraint, String constraintMsg) {
		this.constraint = constraint;
		this.constraintMsg = constraintMsg;
	}
	
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		constraint = (IConditionExpr)ExtUtil.read(in, new ExtWrapTagged(), pf);
		constraintMsg = ExtUtil.nullIfEmpty(ExtUtil.readString(in));
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.write(out, new ExtWrapTagged(constraint));
		ExtUtil.writeString(out, ExtUtil.emptyIfNull(constraintMsg));
	}
}
