package org.javarosa.xpath;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.javarosa.core.model.IFormDataModel;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.IConditionExpr;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.xpath.expr.XPathBinaryOpExpr;
import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.javarosa.xpath.expr.XPathPathExpr;
import org.javarosa.xpath.expr.XPathUnaryOpExpr;
import org.javarosa.xpath.parser.XPathSyntaxException;

public class XPathConditional implements IConditionExpr {
	private String xpath;
	private XPathExpression expr;
	
	public XPathConditional (String xpath) {
		init(xpath);
	}
	
	public XPathConditional () {
		
	}
	
	private void init (String xpath) {
		this.xpath = xpath;
		try {
			expr = XPathParseTool.parseXPath(xpath);
		} catch (XPathSyntaxException e) {
			throw new RuntimeException("condition with invalid xpath: " + xpath);
		}
	}
	
	public String getXPath () {
		return xpath;
	}
	
	public XPathExpression getExpr () {
		return expr;
	}
	
	public boolean eval (IFormDataModel model, EvaluationContext evalContext) {
		return XPathFuncExpr.toBoolean(expr.eval(model, evalContext)).booleanValue();
	}
	
	public Vector getTriggers () {
		Vector v = new Vector();
		getTriggers(expr, v);
		return v;
	}
	
	private void getTriggers (XPathExpression x, Vector v) {
		if (x instanceof XPathPathExpr) {
			v.addElement(((XPathPathExpr)x).getXPath());
		} else if (x instanceof XPathBinaryOpExpr) {
			getTriggers(((XPathBinaryOpExpr)x).a, v);
			getTriggers(((XPathBinaryOpExpr)x).b, v);			
		} else if (x instanceof XPathUnaryOpExpr) {
			getTriggers(((XPathUnaryOpExpr)x).a, v);
		} else if (x instanceof XPathFuncExpr) {
			XPathFuncExpr fx = (XPathFuncExpr)x;
			for (int i = 0; i < fx.args.length; i++)
				getTriggers(fx.args[i], v);
		}
	}
	
	public boolean equals (Object o) {
		if (o instanceof XPathConditional) {
			return xpath.equals(((XPathConditional)o).getXPath());
		} else {
			return false;
		}
	}

	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		init(in.readUTF());
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		out.writeUTF(xpath);
	}
}
