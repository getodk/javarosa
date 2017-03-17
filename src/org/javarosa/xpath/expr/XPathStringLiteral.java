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

public class XPathStringLiteral extends XPathExpression {
	public String s;

	public XPathStringLiteral () { } //for deserialization

	public XPathStringLiteral (String s) {
		this.s = s;
	}
	
	public Object eval (FormInstance model, EvaluationContext evalContext) {
		return s;
	}

	public String toString () {
		return "{str:\'" + s + "\'}"; //TODO: s needs to be escaped (' -> \'; \ -> \\)
	}

	public boolean equals (Object o) {
		if (o instanceof XPathStringLiteral) {
			XPathStringLiteral x = (XPathStringLiteral)o;
			return s.equals(x.s);
		} else {
			return false;
		}
	}

	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		s = ExtUtil.readString(in);
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.writeString(out, s);
	}
}