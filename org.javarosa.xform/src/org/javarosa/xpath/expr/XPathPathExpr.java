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

public class XPathPathExpr extends XPathExpression {
	public static final int INIT_CONTEXT_ROOT = 0;
	public static final int INIT_CONTEXT_RELATIVE = 1;
	public static final int INIT_CONTEXT_EXPR = 2;

	public int init_context;
	public XPathStep[] steps;

	//for INIT_CONTEXT_EXPR only
	public XPathExpression expr;
	public XPathExpression[] predicates;

	public XPathPathExpr (int init_context, XPathStep[] steps) {
		this.init_context = init_context;
		this.steps = steps;
	}

	public XPathPathExpr (XPathExpression expr, XPathExpression[] predicates, XPathStep[] steps) {
		this(INIT_CONTEXT_EXPR, steps);
		this.expr = expr;
		this.predicates = predicates;
	}
	
	public String getXPath () {
		StringBuffer sb = new StringBuffer();
		
		if (init_context == INIT_CONTEXT_EXPR)
			throw new RuntimeException("XPath evaluation: unsupported construct [path expression with expression as root]");
		
		if (init_context == INIT_CONTEXT_ROOT)
			sb.append("/");
		
		for (int i = 0; i < steps.length; i++) {
			if (steps[i].axis != XPathStep.AXIS_CHILD || steps[i].test != XPathStep.TEST_NAME || steps[i].predicates.length > 0)
				throw new RuntimeException("XPath evaluation: unsupported construct [predicates in path expression]");
			
			sb.append(steps[i].name.toString());
			if (i < steps.length - 1)
				sb.append("/");
		}
			
		return sb.toString();
	}
	
	public Object eval (IFormDataModel model) {
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
			throw new RuntimeException("XPath evaluation: cannot handle datatype");
		}
	}

}
