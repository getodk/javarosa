package org.javarosa.core.model.condition;

import java.util.Vector;

import org.javarosa.core.model.IFormDataModel;
import org.javarosa.core.util.externalizable.Externalizable;

public interface IConditionExpr extends Externalizable {
	boolean eval (IFormDataModel model, EvaluationContext evalContext);
	Object evalRaw (IFormDataModel model, EvaluationContext evalContext);
	String evalReadable (IFormDataModel model, EvaluationContext evalContext);
	Vector getTriggers (); /* vector of TreeReference */
}
