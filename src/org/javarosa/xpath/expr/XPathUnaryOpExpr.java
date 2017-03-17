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

import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.core.util.externalizable.DeserializationException;

public abstract class XPathUnaryOpExpr extends XPathOpExpr {
	public XPathExpression a;

	public XPathUnaryOpExpr () { } //for deserialization of children

	public XPathUnaryOpExpr (XPathExpression a) {
		this.a = a;
	}

	public boolean equals (Object o) {
		if (o instanceof XPathUnaryOpExpr) {
			XPathUnaryOpExpr x = (XPathUnaryOpExpr)o;
			return a.equals(x.a);
		} else {
			return false;
		}
	}

	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		a = (XPathExpression)ExtUtil.read(in, new ExtWrapTagged(), pf);
	}
	
	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.write(out, new ExtWrapTagged(a));
	}
}
