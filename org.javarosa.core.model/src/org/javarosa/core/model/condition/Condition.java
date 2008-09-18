package org.javarosa.core.model.condition;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.IFormDataModel;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.utils.ExternalizableHelper;
import org.javarosa.core.util.Externalizable;
import org.javarosa.core.util.UnavailableExternalizerException;

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
	
	private IConditionExpr expr;
	private int trueAction;
	private int falseAction;
	private Vector affectedQuestions;
	
	private Vector qIDs;
	
	public Condition () {
		this(null, ACTION_NULL, ACTION_NULL);
	}
	
	public Condition (IConditionExpr expr, int trueAction, int falseAction) {
		this(expr, trueAction, falseAction, new Vector());
	}
	
	public Condition (IConditionExpr expr, int trueAction, int falseAction, Vector affectedQuestions) {
		this.expr = expr;
		this.trueAction = trueAction;
		this.falseAction = falseAction;
		this.affectedQuestions = affectedQuestions;
	}
	
	public void eval (IFormDataModel model, EvaluationContext evalContext) {
		if (evalContext == null) {
			evalContext = new EvaluationContext();
		}	
		
		boolean result = expr.eval(model, evalContext);
		
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
		return expr.getTriggers();
	}
	
	public boolean equals (Condition c) {
		return (this.trueAction == c.trueAction && this.falseAction == c.falseAction && this.expr.equals(c.expr));
	}
	
	public void readExternal(DataInputStream in) throws IOException,
			InstantiationException, IllegalAccessException,
			UnavailableExternalizerException {
		trueAction = ExternalizableHelper.readNumInt(in, ExternalizableHelper.ENCODING_NUM_DEFAULT);
		falseAction = ExternalizableHelper.readNumInt(in, ExternalizableHelper.ENCODING_NUM_DEFAULT);
		
		//TOTAL HACK!!!
		try {
			expr = (IConditionExpr)Class.forName("org.javarosa.xpath.XPathConditional").newInstance();
		} catch (ClassNotFoundException cnfe) {
			throw new RuntimeException("can't find org.javarosa.xpath.XPathConditional... moved?");
		}
		expr.readExternal(in);
		
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
		expr.writeExternal(out);
		
		//affected q's
		qIDs = new Vector();
		for (int i = 0; i < affectedQuestions.size(); i++)
			qIDs.addElement(new Integer(((QuestionDef)affectedQuestions.elementAt(i)).getID()));
		ExternalizableHelper.writeIntegers(qIDs, out);
	}

	
	
}