package org.javarosa.xpath.expr;

import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.IFormDataModel;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.Selection;
import org.javarosa.core.model.data.StringData;
import org.javarosa.model.xform.XPathReference;
import org.javarosa.xform.util.XFormAnswerDataSerializer;
import org.javarosa.xpath.EvaluationContext;
import org.javarosa.xpath.XPathUnsupportedException;

public class XPathPathExpr extends XPathExpression {
	public static final int INIT_CONTEXT_ROOT = 0;
	public static final int INIT_CONTEXT_RELATIVE = 1;
	public static final int INIT_CONTEXT_EXPR = 2;

	public int init_context;
	public XPathStep[] steps;

	//for INIT_CONTEXT_EXPR only
	public XPathFilterExpr filtExpr;

	public XPathPathExpr (int init_context, XPathStep[] steps) {
		this.init_context = init_context;
		this.steps = steps;
	}

	public XPathPathExpr (XPathFilterExpr filtExpr, XPathStep[] steps) {
		this(INIT_CONTEXT_EXPR, steps);
		this.filtExpr = filtExpr;
	}
	
	public String getXPath () {
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
			
		return sb.toString();
	}
	
	public Object eval (IFormDataModel model, EvaluationContext evalContext) {
		String xpathRef = getXPath();
		IDataReference ref = new XPathReference(xpathRef);
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
}
