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
import java.util.Arrays;
import java.util.List;

import org.javarosa.core.util.CacheTable;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapListPoly;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

public class XPathStep implements Externalizable {
	public static final int AXIS_CHILD = 0;
	public static final int AXIS_DESCENDANT = 1;
	public static final int AXIS_PARENT = 2;
	public static final int AXIS_ANCESTOR = 3;
	public static final int AXIS_FOLLOWING_SIBLING = 4;
	public static final int AXIS_PRECEDING_SIBLING = 5;
	public static final int AXIS_FOLLOWING = 6;
	public static final int AXIS_PRECEDING = 7;
	public static final int AXIS_ATTRIBUTE = 8;
	public static final int AXIS_NAMESPACE = 9;
	public static final int AXIS_SELF = 10;
	public static final int AXIS_DESCENDANT_OR_SELF = 11;
	public static final int AXIS_ANCESTOR_OR_SELF = 12;

	public static final int TEST_NAME = 0;
	public static final int TEST_NAME_WILDCARD = 1;
	public static final int TEST_NAMESPACE_WILDCARD = 2;
	public static final int TEST_TYPE_NODE = 3;
	public static final int TEST_TYPE_TEXT = 4;
	public static final int TEST_TYPE_COMMENT = 5;
	public static final int TEST_TYPE_PROCESSING_INSTRUCTION = 6;

	private static CacheTable<XPathStep> refs;

	public static void attachCacheTable(CacheTable<XPathStep> refs) {
		XPathStep.refs = refs;
	}

	public static XPathStep ABBR_SELF () {
		return new XPathStep(AXIS_SELF, TEST_TYPE_NODE);
	}

	public static XPathStep ABBR_PARENT () {
		return new XPathStep(AXIS_PARENT, TEST_TYPE_NODE);
	}

	public static XPathStep ABBR_DESCENDANTS () {
		return new XPathStep(AXIS_DESCENDANT_OR_SELF, TEST_TYPE_NODE);
	}

	public int axis;
	public int test;
	public XPathExpression[] predicates;

	//test-dependent variables
	public XPathQName name; //TEST_NAME only
	public String namespace; //TEST_NAMESPACE_WILDCARD only
	public String literal; //TEST_TYPE_PROCESSING_INSTRUCTION only

	public XPathStep () { } //for deserialization

	public XPathStep (int axis, int test) {
		this.axis = axis;
		this.test = test;
		this.predicates = new XPathExpression[0];
	}

	public XPathStep (int axis, XPathQName name) {
		this(axis, TEST_NAME);
		this.name = name;
	}

	public XPathStep (int axis, String namespace) {
		this(axis, TEST_NAMESPACE_WILDCARD);
		this.namespace = namespace;
	}

	public String toString () {
		StringBuilder sb = new StringBuilder();

		sb.append("{step:");
		sb.append(axisStr(axis));
		sb.append(",");
		sb.append(testStr());

		if (predicates.length > 0) {
			sb.append(",{");
			for (int i = 0; i < predicates.length; i++) {
				sb.append(predicates[i].toString());
				if (i < predicates.length - 1)
					sb.append(",");
			}
			sb.append("}");
		}
		sb.append("}");

		return sb.toString();
	}

	public static String axisStr (int axis) {
		switch (axis) {
		case AXIS_CHILD: return "child";
		case AXIS_DESCENDANT: return "descendant";
		case AXIS_PARENT: return "parent";
		case AXIS_ANCESTOR: return "ancestor";
		case AXIS_FOLLOWING_SIBLING: return "following-sibling";
		case AXIS_PRECEDING_SIBLING: return "preceding-sibling";
		case AXIS_FOLLOWING: return "following";
		case AXIS_PRECEDING: return "preceding";
		case AXIS_ATTRIBUTE: return "attribute";
		case AXIS_NAMESPACE: return "namespace";
		case AXIS_SELF: return "self";
		case AXIS_DESCENDANT_OR_SELF: return "descendant-or-self";
		case AXIS_ANCESTOR_OR_SELF: return "ancestor-or-self";
		default: return null;
		}
	}

	public String testStr () {
		switch(test) {
		case TEST_NAME: return name.toString();
		case TEST_NAME_WILDCARD: return "*";
		case TEST_NAMESPACE_WILDCARD: return namespace + ":*";
		case TEST_TYPE_NODE: return "node()";
		case TEST_TYPE_TEXT: return "text()";
		case TEST_TYPE_COMMENT: return "comment()";
		case TEST_TYPE_PROCESSING_INSTRUCTION: return "proc-instr(" + (literal == null ? "" : "\'" + literal + "\'") + ")";
		default: return null;
		}
	}

	public boolean equals (Object o) {
		if (o instanceof XPathStep) {
			XPathStep x = (XPathStep)o;

			//shortcuts for faster evaluation
			if(axis != x.axis || test != x.test || predicates.length != x.predicates.length) {
				return false;
			}

			switch (test) {
			case TEST_NAME: if(!name.equals(x.name)) {return false;} break;
			case TEST_NAMESPACE_WILDCARD: if(!namespace.equals(x.namespace)) {return false;} break;
			case TEST_TYPE_PROCESSING_INSTRUCTION: if(!ExtUtil.equals(literal, x.literal)) {return false;} break;
			default: break;
			}

			return ExtUtil.arrayEquals(predicates, x.predicates);
		} else {
			return false;
		}
	}

	/**
	 * "matches" follows roughly the same process as equals(), in that it for a step it will
	 * check whether two steps share the same properties (multiplicity, test, axis, etc).
	 * The only difference is that match() will allow for a named step to match a step who's name
	 * is a wildcard.
	 *
	 * So
	 * \/path\/
	 * will "match"
	 * \/*\/
	 *
	 * even though they are not equal.
	 *
	 * Matching is reflexive, consistent, and symmetric, but _not_ transitive.

	 * @param xPathStep
	 * @return
	 */
	protected boolean matches(XPathStep o) {
		if (o instanceof XPathStep) {
			XPathStep x = (XPathStep)o;

			//shortcuts for faster evaluation
			if(axis != x.axis || (test != x.test && !((x.test == TEST_NAME && this.test == TEST_NAME_WILDCARD)||(this.test==TEST_NAME && x.test == TEST_NAME_WILDCARD))) || predicates.length != x.predicates.length) {
				return false;
			}

			switch (test) {
			case TEST_NAME: if(x.test != TEST_NAME_WILDCARD && !name.equals(x.name)) {return false;} break;
			case TEST_NAMESPACE_WILDCARD: if(!namespace.equals(x.namespace)) {return false;} break;
			case TEST_TYPE_PROCESSING_INSTRUCTION: if(!ExtUtil.equals(literal, x.literal)) {return false;} break;
			default: break;
			}

			return ExtUtil.arrayEquals(predicates, x.predicates);
		} else {
			return false;
		}
	}

	public int hashCode() {
		int code = this.axis ^ this.test ^ (this.name == null ? 0 : this.name.hashCode()) ^ (this.literal == null ? 0 : this.literal.hashCode()) ^ (this.namespace == null ? 0 : this.namespace.hashCode());
		for(XPathExpression xpe : predicates) {
			code ^= xpe.hashCode();
		}
		return code;
	}

	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		axis = ExtUtil.readInt(in);
		test = ExtUtil.readInt(in);

		switch (test) {
		case TEST_NAME: name = (XPathQName)ExtUtil.read(in, XPathQName.class); break;
		case TEST_NAMESPACE_WILDCARD: namespace = ExtUtil.readString(in); break;
		case TEST_TYPE_PROCESSING_INSTRUCTION: literal = (String)ExtUtil.read(in, new ExtWrapNullable(String.class)); break;
		}

      List<Object> v = (List<Object>)ExtUtil.read(in, new ExtWrapListPoly(), pf);
		predicates = new XPathExpression[v.size()];
		for (int i = 0; i < predicates.length; i++)
			predicates[i] = (XPathExpression)v.get(i);
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.writeNumeric(out, axis);
		ExtUtil.writeNumeric(out, test);

		switch (test) {
		case TEST_NAME: ExtUtil.write(out, name); break;
		case TEST_NAMESPACE_WILDCARD: ExtUtil.writeString(out, namespace); break;
		case TEST_TYPE_PROCESSING_INSTRUCTION: ExtUtil.write(out, new ExtWrapNullable(literal)); break;
		}

      List<XPathExpression> v = Arrays.asList(predicates);
      
		ExtUtil.write(out, new ExtWrapListPoly(v));
	}

	public static boolean XPathStepInterningEnabled = true;
	public XPathStep intern() {
		if(!XPathStepInterningEnabled || refs == null) {
			return this;
		} else{
			return refs.intern(this);
		}
	}
}
