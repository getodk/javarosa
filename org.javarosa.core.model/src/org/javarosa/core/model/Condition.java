package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.javarosa.core.model.utils.ExternalizableHelper;
import org.javarosa.core.util.Externalizable;
import org.javarosa.core.util.UnavailableExternalizerException;
import org.javarosa.xpath.EvaluationContext;
import org.javarosa.xpath.XPathTest;
import org.javarosa.xpath.expr.XPathBinaryOpExpr;
import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.javarosa.xpath.expr.XPathPathExpr;
import org.javarosa.xpath.expr.XPathUnaryOpExpr;
import org.javarosa.xpath.parser.XPathSyntaxException;

public class Condition implements Externalizable {
	public static final int ACTION_NULL = 0;
	public static final int ACTION_SHOW = 1;
	public static final int ACTION_HIDE = 2;
	public static final int ACTION_ENABLE = 3;
	public static final int ACTION_DISABLE = 4;
	public static final int ACTION_LOCK = 5;
	public static final int ACTION_UNLOCK = 6;
	public static final int ACTION_REQUIRE = 7;
	public static final int ACTION_DONT_REQUIRE = 8;
	
	private XPathExpression expr;
	private int trueAction;
	private int falseAction;
	private Vector affectedQuestions;
	
	//hackiness for now to ease comparison and serialization
	public String xpath; 
	private Vector qIDs;
	
	public Condition () {
		this(null, ACTION_NULL, ACTION_NULL);
	}
	
	public Condition (XPathExpression expr, int trueAction, int falseAction) {
		this(expr, trueAction, falseAction, new Vector());
	}
	
	public Condition (XPathExpression expr, int trueAction, int falseAction, Vector affectedQuestions) {
		this.expr = expr;
		this.trueAction = trueAction;
		this.falseAction = falseAction;
		this.affectedQuestions = affectedQuestions;
	}
	
	public void eval (IFormDataModel model, EvaluationContext evalContext) {
		if (evalContext == null) {
			evalContext = new EvaluationContext();
		}	
			
		boolean result = XPathFuncExpr.toBoolean(expr.eval(model, evalContext)).booleanValue();
		
		for (int i = 0; i < affectedQuestions.size(); i++) {
			performAction((QuestionDef)affectedQuestions.elementAt(i), result ? trueAction : falseAction);
		}
	}

	private void performAction (QuestionDef q, int action) {
		switch (action) {
		case ACTION_NULL: break;
		case ACTION_SHOW:         q.setVisible(true); break;
		case ACTION_HIDE:         q.setVisible(false); break;
		case ACTION_ENABLE:       q.setEnabled(true); break;
		case ACTION_DISABLE:      q.setEnabled(false); break;
		case ACTION_LOCK:         q.setLocked(true); break;
		case ACTION_UNLOCK:       q.setLocked(false); break;
		case ACTION_REQUIRE:      q.setRequired(true); break;
		case ACTION_DONT_REQUIRE: q.setRequired(false); break;
		}
	}
	
	public void addAffectedQuestion (QuestionDef question) {
		affectedQuestions.addElement(question);
	}
	
	public Vector getAffectedQuestions () {
		return affectedQuestions;
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
	
	public boolean equals (Condition c) {
		return (this.trueAction == c.trueAction && this.falseAction == c.falseAction && this.xpath.equals(c.xpath));
	}
	
	public void readExternal(DataInputStream in) throws IOException,
			InstantiationException, IllegalAccessException,
			UnavailableExternalizerException {
		trueAction = ExternalizableHelper.readNumInt(in, ExternalizableHelper.ENCODING_NUM_DEFAULT);
		falseAction = ExternalizableHelper.readNumInt(in, ExternalizableHelper.ENCODING_NUM_DEFAULT);
		xpath = ExternalizableHelper.readUTF(in);
		try {
			expr = XPathTest.parseXPath(xpath);
		} catch (XPathSyntaxException xse) { }
		
		//affected q's
		qIDs = ExternalizableHelper.readIntegers(in);
		//can't convert qIDs to QuestionDefs until 'form' is set by FormDef; thus attachForm, below
	}

	public void attachForm (FormDef form) {
		if (qIDs != null) {
			for (int i = 0; i < qIDs.size(); i++)
				affectedQuestions.addElement(form.getQuesitonByID(((Integer)qIDs.elementAt(i)).intValue()));
		}
	}
	
	public void writeExternal(DataOutputStream out) throws IOException {
		ExternalizableHelper.writeNumeric(out, trueAction, ExternalizableHelper.ENCODING_NUM_DEFAULT);
		ExternalizableHelper.writeNumeric(out, falseAction, ExternalizableHelper.ENCODING_NUM_DEFAULT);
		ExternalizableHelper.writeUTF(out, xpath);
		//don't write expr; will rebuild from xpath
		
		//affected q's
		qIDs = new Vector();
		for (int i = 0; i < affectedQuestions.size(); i++)
			qIDs.addElement(new Integer(((QuestionDef)affectedQuestions.elementAt(i)).getID()));
		ExternalizableHelper.writeIntegers(qIDs, out);
	}

	
	
}