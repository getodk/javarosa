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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.pivot.UnpivotableExpressionException;
import org.javarosa.core.model.data.BooleanData;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.GeoTraceData;
import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.core.model.data.GeoShapeData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.LongData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.data.UncastData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapList;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.xform.util.XFormAnswerDataSerializer;
import org.javarosa.xpath.XPathException;
import org.javarosa.xpath.XPathMissingInstanceException;
import org.javarosa.xpath.XPathNodeset;
import org.javarosa.xpath.XPathTypeMismatchException;
import org.javarosa.xpath.XPathUnsupportedException;

public class XPathPathExpr extends XPathExpression {
	public static final int INIT_CONTEXT_ROOT = 0;
	public static final int INIT_CONTEXT_RELATIVE = 1;
	public static final int INIT_CONTEXT_EXPR = 2;

	public int init_context;
	public XPathStep[] steps;

	//for INIT_CONTEXT_EXPR only
	public XPathFilterExpr filtExpr;

	public XPathPathExpr () { } //for deserialization

	public XPathPathExpr (int init_context, XPathStep[] steps) {
		this.init_context = init_context;
		this.steps = steps;
	}

	public XPathPathExpr (XPathFilterExpr filtExpr, XPathStep[] steps) {
		this(INIT_CONTEXT_EXPR, steps);
		this.filtExpr = filtExpr;
	}

	public TreeReference getReference () throws XPathUnsupportedException {
		return getReference(false);
	}

	/**
	 * translate an xpath path reference into a TreeReference
	 * TreeReferences only support a subset of true xpath paths; restrictions are:
	 *   simple child name tests 'child::name', '.', and '..' allowed only
	 *   no predicates
	 *   all '..' steps must come before anything else
	 */
	public TreeReference getReference (boolean allowPredicates) throws XPathUnsupportedException {
		TreeReference ref = new TreeReference();
		boolean parentsAllowed;
		switch (init_context) {
		case XPathPathExpr.INIT_CONTEXT_ROOT:
			ref.setRefLevel(TreeReference.REF_ABSOLUTE);
			parentsAllowed = false;
			break;
		case XPathPathExpr.INIT_CONTEXT_RELATIVE:
			ref.setRefLevel(0);
			parentsAllowed = true;
			break;
		case XPathPathExpr.INIT_CONTEXT_EXPR:
			if (this.filtExpr.x != null && this.filtExpr.x instanceof XPathFuncExpr)
			{
				XPathFuncExpr func = (XPathFuncExpr)(this.filtExpr.x);
				if(func.id.toString().equals("instance"))
				{
					ref.setRefLevel(TreeReference.REF_ABSOLUTE); //i assume when refering the non main instance you have to be absolute
					parentsAllowed = false;
					if(func.args.length != 1)
					{
						throw new XPathUnsupportedException("instance() function used with "+func.args.length+ " arguements. Expecting 1 arguement");
					}
					if(!(func.args[0] instanceof XPathStringLiteral))
					{
						throw new XPathUnsupportedException("instance() function expecting 1 string literal arguement arguement");
					}
					XPathStringLiteral strLit = (XPathStringLiteral)(func.args[0]);
					//we've got a non-standard instance in play, watch out
					if(strLit.s == null) {
						// absolute reference to the main instance
						ref.setContext(TreeReference.CONTEXT_ABSOLUTE);
						ref.setInstanceName(null);
					} else{
						ref.setContext(TreeReference.CONTEXT_INSTANCE);
						ref.setInstanceName(strLit.s);
					}
				} else if(func.id.toString().equals("current")){
					parentsAllowed = true;
					ref.setContext(TreeReference.CONTEXT_ORIGINAL);
				} else {
					//We only support expression root contexts for instance refs, everything else is an illegal filter
					throw new XPathUnsupportedException("filter expression");
				}
			} else {
				//We only support expression root contexts for instance refs, everything else is an illegal filter
				throw new XPathUnsupportedException("filter expression");
			}

			break;
		default: throw new XPathUnsupportedException("filter expression");
		}
		for (int i = 0; i < steps.length; i++) {
			XPathStep step = steps[i];
			if (step.axis == XPathStep.AXIS_SELF) {
				if (step.test != XPathStep.TEST_TYPE_NODE) {
					throw new XPathUnsupportedException("step other than 'child::name', '.', '..'");
				}
			} else if (step.axis == XPathStep.AXIS_PARENT) {
				if (!parentsAllowed || step.test != XPathStep.TEST_TYPE_NODE) {
					throw new XPathUnsupportedException("step other than 'child::name', '.', '..'");
				} else {
					ref.incrementRefLevel();
				}
			} else if (step.axis == XPathStep.AXIS_ATTRIBUTE) {
				if (step.test == XPathStep.TEST_NAME) {
					ref.add(step.name.toString(), TreeReference.INDEX_ATTRIBUTE);
					parentsAllowed = false;
					//TODO: Can you step back from an attribute, or should this always be
					//the last step?
				} else {
					throw new XPathUnsupportedException("attribute step other than 'attribute::name");
				}
			}else if (step.axis == XPathStep.AXIS_CHILD) {
				if (step.test == XPathStep.TEST_NAME) {
					ref.add(step.name.toString(), TreeReference.INDEX_UNBOUND);
					parentsAllowed = true;
				} else if(step.test == XPathStep.TEST_NAME_WILDCARD) {
					ref.add(TreeReference.NAME_WILDCARD, TreeReference.INDEX_UNBOUND);
					parentsAllowed = true;
				} else {
					throw new XPathUnsupportedException("step other than 'child::name', '.', '..'");
				}
			} else {
				throw new XPathUnsupportedException("step other than 'child::name', '.', '..'");
			}

			if(step.predicates.length > 0) {
				int refLevel = ref.getRefLevel();
            List<XPathExpression> v = new ArrayList<XPathExpression>(step.predicates.length);
				for(int j = 0; j < step.predicates.length; j++)
				{
					v.add(step.predicates[j]);
				}
				ref.addPredicate(i, v);
			}
		}
		return ref;
	}

	public XPathNodeset eval (FormInstance m, EvaluationContext ec) {
		TreeReference genericRef = getReference();

		TreeReference ref;
		if(genericRef.getContext() == TreeReference.CONTEXT_ORIGINAL) {
			ref = genericRef.contextualize(ec.getOriginalContext());
		} else {
			ref = genericRef.contextualize(ec.getContextRef());
		}

		//We don't necessarily know the model we want to be working with until we've contextualized the
		//node

		//check if this nodeset refers to a non-main instance
		if(ref.getInstanceName() != null && ref.isAbsolute())
		{
			FormInstance nonMain = ec.getInstance(ref.getInstanceName());
			if(nonMain != null)
			{
				m = nonMain;
			}
			else
			{
				throw new XPathMissingInstanceException(ref.getInstanceName(), "Instance referenced by " + ref.toString(true) + " does not exist");
			}
		} else {
            //TODO: We should really stop passing 'm' around and start just getting the right instance from ec
            //at a more central level
            m = ec.getMainInstance();

            if(m == null) {
                    String refStr = ref == null ? "" : ref.toString(true);
    				throw new XPathException("Cannot evaluate the reference [" + refStr + "] in the current evaluation context. No default instance has been declared!");
            }
		}

		// regardless of the above, we want to ensure there is a definition
		if(m.getRoot() == null) {
			//This instance is _declared_, but doesn't actually have any data in it.
			throw new XPathMissingInstanceException(ref.getInstanceName(), "Instance referenced by " + ref.toString(true) + " has not been loaded");
		}

		// this makes no sense...
//		if (ref.isAbsolute() && m.getTemplatePath(ref) == null) {
//			List<TreeReference> nodesetRefs = new List<TreeReference>();
//			return new XPathNodeset(nodesetRefs, m, ec);
//		}

      List<TreeReference> nodesetRefs = ec.expandReference(ref);

		//to fix conditions based on non-relevant data, filter the nodeset by relevancy
		for (int i = 0; i < nodesetRefs.size(); i++) {
			if (!m.resolveReference(nodesetRefs.get(i)).isRelevant()) {
				nodesetRefs.remove(i);
				i--;
			}
		}

		return new XPathNodeset(nodesetRefs, m, ec);
	}

//
//	boolean nodeset = forceNodeset;
//	if (!nodeset) {
//		//is this a nodeset? it is if the ref contains any unbound multiplicities AND the unbound nodes are repeatable
//		//the way i'm calculating this sucks; there has got to be an easier way to find out if a node is repeatable
//		TreeReference repeatTestRef = TreeReference.rootRef();
//		for (int i = 0; i < ref.size(); i++) {
//			repeatTestRef.add(ref.getName(i), ref.getMultiplicity(i));
//			if (ref.getMultiplicity(i) == TreeReference.INDEX_UNBOUND) {
//				if (m.getTemplate(repeatTestRef) != null) {
//					nodeset = true;
//					break;
//				}
//			}
//		}
//	}

	public static Object getRefValue (FormInstance model, EvaluationContext ec, TreeReference ref) {
		if (ec.isConstraint && ref.equals(ec.getContextRef())) {
			//ITEMSET TODO: need to update this; for itemset/copy constraints, need to simulate a whole xml sub-tree here
			return unpackValue(ec.candidateValue);
		} else {
			TreeElement node = model.resolveReference(ref);
			if (node == null) {
				//shouldn't happen -- only existent nodes should be in nodeset
				throw new XPathTypeMismatchException("Node " + ref.toString() + " does not exist!");
			}

			return unpackValue(node.isRelevant() ? node.getValue() : null);
		}
	}

	public static Object unpackValue (IAnswerData val) {
		if (val == null) {
			return "";
		} else if (val instanceof UncastData) {
			return val.getValue();
		} else if (val instanceof IntegerData) {
			return new Double(((Integer)val.getValue()).doubleValue());
		} else if (val instanceof LongData) {
			return new Double(((Long)val.getValue()).doubleValue());
		} else if (val instanceof DecimalData) {
			return val.getValue();
		} else if (val instanceof StringData) {
			return val.getValue();
		} else if (val instanceof SelectOneData) {
			return ((Selection)val.getValue()).getValue();
		} else if (val instanceof SelectMultiData) {
			return (new XFormAnswerDataSerializer()).serializeAnswerData(val);
		} else if (val instanceof DateData) {
			return val.getValue();
		} else if (val instanceof BooleanData) {
			return val.getValue();
		} else if (val instanceof GeoPointData) {
			// we have no access fns that interact with double[4] arrays (the getValue() data type)...
			return val.getDisplayText();
		} else if (val instanceof GeoShapeData) {
			// we have no access fns that interact with GeoShape objects (the getValue() data type)...
			return val.getDisplayText();
		} else if (val instanceof GeoTraceData) {
			// we have no access fns that interact with GeoTrace objects (the getValue() data type)...
			return val.getDisplayText();
		} else {
			System.out.println("warning: unrecognized data type in xpath expr: " + val.getClass().getName());
			return val.getValue(); //is this a good idea?
		}
	}

	public String toString () {
		StringBuilder sb = new StringBuilder();

		sb.append("{path-expr:");
		switch (init_context) {
		case INIT_CONTEXT_ROOT: sb.append("abs"); break;
		case INIT_CONTEXT_RELATIVE: sb.append("rel"); break;
		case INIT_CONTEXT_EXPR: sb.append(filtExpr.toString()); break;
		}
		sb.append(",{");
		for (int i = 0; i < steps.length; i++) {
			sb.append(steps[i].toString());
			if (i < steps.length - 1)
				sb.append(",");
		}
		sb.append("}}");

		return sb.toString();
	}

	public boolean equals (Object o) {
		if (o instanceof XPathPathExpr) {
			XPathPathExpr x = (XPathPathExpr)o;

			//Shortcuts for easily comparable values
			if(init_context != x.init_context || steps.length != x.steps.length) {
				return false;
			}

			return ExtUtil.arrayEquals(steps, x.steps) && (init_context == INIT_CONTEXT_EXPR ? filtExpr.equals(x.filtExpr) : true);
		} else {
			return false;
		}
	}

	/**
	 * Warning: this method has somewhat unclear semantics.
	 *
	 * "matches" follows roughly the same process as equals(), in that it goes
	 * through the path step by step and compares whether each step can refer to the same node.
	 * The only difference is that match() will allow for a named step to match a step who's name
	 * is a wildcard.
	 *
	 * So
	 * \/data\/path\/to
	 * will "match"
	 * \/data\/*\/to
	 *
	 * even though they are not equal.
	 *
	 * Matching is reflexive, consistent, and symmetric, but _not_ transitive.
	 *
	 * @param o
	 * @return true if the expression is a path that matches this one
	 */
	public boolean matches(XPathExpression o) {
		if (o instanceof XPathPathExpr) {
			XPathPathExpr x = (XPathPathExpr)o;

			//Shortcuts for easily comparable values
			if(init_context != x.init_context || steps.length != x.steps.length) {
				return false;
			}

			if (steps.length != x.steps.length) {
				return false;
			} else {
				for (int i = 0; i < steps.length; i++) {
					if (!steps[i].matches(x.steps[i])) {
						return false;
					}
				}
			}

			// If all steps match, we still need to make sure we're in the same "context" if this
			// is a normal expression.
			return (init_context == INIT_CONTEXT_EXPR ? filtExpr.equals(x.filtExpr) : true);
		} else {
			return false;
		}
	}

	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		init_context = ExtUtil.readInt(in);
		if (init_context == INIT_CONTEXT_EXPR) {
			filtExpr = (XPathFilterExpr)ExtUtil.read(in, XPathFilterExpr.class, pf);
		}

      List<Object> v = (List<Object>)ExtUtil.read(in, new ExtWrapList(XPathStep.class), pf);
		steps = new XPathStep[v.size()];
		for (int i = 0; i < steps.length; i++)
			steps[i] = ((XPathStep)v.get(i)).intern();
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.writeNumeric(out, init_context);
		if (init_context == INIT_CONTEXT_EXPR) {
			ExtUtil.write(out, filtExpr);
		}

      List<XPathStep> v = Arrays.asList(steps);
		ExtUtil.write(out, new ExtWrapList(v));
	}

	public static XPathPathExpr fromRef (TreeReference ref) {
		XPathPathExpr path = new XPathPathExpr();
		path.init_context = (ref.isAbsolute() ? INIT_CONTEXT_ROOT : INIT_CONTEXT_RELATIVE);
		path.steps = new XPathStep[ref.size()];
		for (int i = 0; i < path.steps.length; i++) {
			if (ref.getName(i).equals(TreeReference.NAME_WILDCARD)) {
				path.steps[i] = new XPathStep(XPathStep.AXIS_CHILD, XPathStep.TEST_NAME_WILDCARD).intern();
			} else {
				path.steps[i] = new XPathStep(XPathStep.AXIS_CHILD, new XPathQName(ref.getName(i))).intern();
			}
		}
		return path;
	}

	public Object pivot (FormInstance model, EvaluationContext evalContext, List<Object> pivots, Object sentinal) throws UnpivotableExpressionException {
		TreeReference ref = this.getReference();
		//Either concretely the sentinal, or "."
		if(ref.equals(sentinal) || (ref.getRefLevel() == 0)) {
			return sentinal;
		}
		else {
			//It's very, very hard to figure out how to pivot predicates. For now, just skip it
			for(int i = 0 ; i < ref.size(); ++i) {
				if(ref.getPredicate(i) != null && ref.getPredicate(i).size() > 0) {
					throw new UnpivotableExpressionException("Can't pivot filtered treereferences. Ref: " + ref.toString(true) + " has predicates.");
				}
			}
			return this.eval(model, evalContext);
		}
	}
}