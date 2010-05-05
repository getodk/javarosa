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
import java.util.Vector;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.data.BooleanData;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapList;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.xform.util.XFormAnswerDataSerializer;
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
		default: throw new XPathUnsupportedException("filter expression");
		}
		
		for (int i = 0; i < steps.length; i++) {
			XPathStep step = steps[i];
			
			if (!allowPredicates && step.predicates.length > 0) {
				throw new XPathUnsupportedException("predicates");
			}
			
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
					parentsAllowed = false;
				} else if(step.test == XPathStep.TEST_NAME_WILDCARD) {
					ref.add(TreeReference.NAME_WILDCARD, TreeReference.INDEX_UNBOUND);
					parentsAllowed = false;
				} else {
					throw new XPathUnsupportedException("step other than 'child::name', '.', '..'");
				}
			} else {
				throw new XPathUnsupportedException("step other than 'child::name', '.', '..'");
			}
		}		
		
		return ref;
	}

	public XPathNodeset eval (FormInstance m, EvaluationContext evalContext) {
		TreeReference genericRef = getReference();
		if (m.getTemplatePath(genericRef) == null) {
			throw new XPathTypeMismatchException("Node " + genericRef.toString() + " does not exist!");
		}
		
		TreeReference ref = genericRef.contextualize(evalContext.getContextRef());
		Vector<TreeReference> nodesetRefs = m.expandReference(ref);
		
		//to fix conditions based on non-relevant data, filter the nodeset by relevancy
		for (int i = 0; i < nodesetRefs.size(); i++) {
			if (!m.resolveReference((TreeReference)nodesetRefs.elementAt(i)).isRelevant()) {
				nodesetRefs.removeElementAt(i);
				i--;
			}
		}
		
		return new XPathNodeset(nodesetRefs, m, evalContext);
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
		} else if (val instanceof IntegerData) {
			return new Double(((Integer)val.getValue()).doubleValue());
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
		} else {
			System.out.println("warning: unrecognized data type in xpath expr: " + val.getClass().getName());
			return val.getValue(); //is this a good idea?
		}
	}
	
	public String toString () {
		StringBuffer sb = new StringBuffer();
		
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
	
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		init_context = ExtUtil.readInt(in);
		if (init_context == INIT_CONTEXT_EXPR) {
			filtExpr = (XPathFilterExpr)ExtUtil.read(in, XPathFilterExpr.class, pf);
		}
		
		Vector v = (Vector)ExtUtil.read(in, new ExtWrapList(XPathStep.class), pf);
		steps = new XPathStep[v.size()];
		for (int i = 0; i < steps.length; i++)
			steps[i] = (XPathStep)v.elementAt(i);
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.writeNumeric(out, init_context);
		if (init_context == INIT_CONTEXT_EXPR) {
			ExtUtil.write(out, filtExpr);
		}
		
		Vector v = new Vector();
		for (int i = 0; i < steps.length; i++)
			v.addElement(steps[i]);
		ExtUtil.write(out, new ExtWrapList(v));
	}
	
	public static XPathPathExpr fromRef (TreeReference ref) {
		XPathPathExpr path = new XPathPathExpr();
		path.init_context = (ref.isAbsolute() ? INIT_CONTEXT_ROOT : INIT_CONTEXT_RELATIVE);
		path.steps = new XPathStep[ref.size()];
		for (int i = 0; i < path.steps.length; i++) {
			if (ref.getName(i).equals(TreeReference.NAME_WILDCARD)) {
				path.steps[i] = new XPathStep(XPathStep.AXIS_CHILD, XPathStep.TEST_NAME_WILDCARD);
			} else {
				path.steps[i] = new XPathStep(XPathStep.AXIS_CHILD, new XPathQName(ref.getName(i)));
			}
		}
		return path;
	}
}
