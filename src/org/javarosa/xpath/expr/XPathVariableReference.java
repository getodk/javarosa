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

package org.javarosa.xpath.expr;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;

public class XPathVariableReference extends XPathExpression {
    public XPathQName id;

	public XPathVariableReference () { } //for deserialization

    public XPathVariableReference (XPathQName id) {
    	this.id = id;
    }
    
	public Object eval (FormInstance model, EvaluationContext evalContext) {
		return evalContext.getVariable(id.toString());
	}

	public String toString () {
		return "{var:" + id.toString() + "}";
	}
	
	public boolean equals (Object o) {
		if (o instanceof XPathVariableReference) {
			XPathVariableReference x = (XPathVariableReference)o;
			return id.equals(x.id);
		} else {
			return false;
		}
	}
	
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		id = (XPathQName)ExtUtil.read(in, XPathQName.class);
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.write(out, id);
	}
}
