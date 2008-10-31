package org.javarosa.xpath.expr;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.IFormDataModel;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.Selection;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapList;
import org.javarosa.core.util.externalizable.ExtWrapListPoly;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.model.xform.XPathReference;
import org.javarosa.xform.util.XFormAnswerDataSerializer;
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
	
	public XPathReference getXPath () {
		StringBuffer sb = new StringBuffer();
		
		if (init_context == INIT_CONTEXT_EXPR)
			throw new XPathUnsupportedException("path expression with expression as root");
		if (init_context == INIT_CONTEXT_RELATIVE)
			throw new XPathUnsupportedException("relative path expression");		
		
		if (init_context == INIT_CONTEXT_ROOT)
			sb.append("/");
		
		for (int i = 0; i < steps.length; i++) {
			if (steps[i].axis != XPathStep.AXIS_CHILD)
				throw new XPathUnsupportedException("non-child axis");		
			if (steps[i].test != XPathStep.TEST_NAME)
				throw new XPathUnsupportedException("node test other than name");				
			if (steps[i].predicates.length > 0)
				throw new XPathUnsupportedException("predicates in path expression");
			
			sb.append(steps[i].name.toString());
			if (i < steps.length - 1)
				sb.append("/");
		}
			
		return new XPathReference(sb.toString());
	}
	
	public Object eval (IFormDataModel model, EvaluationContext evalContext) {
		IDataReference ref = getXPath();
		IAnswerData val = model.getDataValue(ref);
		
		if (val == null) {
			return "";
		} else if (val instanceof IntegerData) {
			return new Double(((Integer)val.getValue()).doubleValue());
		} else if (val instanceof StringData) {
			return val.getValue();
		} else if (val instanceof SelectOneData) {
			return ((Selection)val.getValue()).getValue();
		} else if (val instanceof SelectMultiData) {
			return (new XFormAnswerDataSerializer()).serializeAnswerData(val);
		} else if (val instanceof DateData) {
			return val.getValue();
		} else {
			return val.getValue();
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

			Vector a = new Vector();
			for (int i = 0; i < steps.length; i++)
				a.addElement(steps[i]);
			Vector b = new Vector();
			for (int i = 0; i < x.steps.length; i++)
				b.addElement(x.steps[i]);
			
			return init_context == x.init_context && ExtUtil.vectorEquals(a, b) &&
				(init_context == INIT_CONTEXT_EXPR ? filtExpr.equals(x.filtExpr) : true);
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
}
