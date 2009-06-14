package org.javarosa.core.model.condition;

import java.util.Date;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.IFormDataModel;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.TreeReference;

public class Recalculate extends Triggerable {
	public Recalculate () {

	}
	
	public Recalculate (IConditionExpr expr, TreeReference contextRef) {
		super(expr, contextRef);
	}
	
	public Recalculate (IConditionExpr expr, TreeReference target, TreeReference contextRef) {
		super(expr, contextRef);
		addTarget(target);
	}
	
	public Object eval (IFormDataModel model, EvaluationContext ec) {
		return expr.evalRaw(model, ec);
	}
	
	public void apply (TreeReference ref, Object result, IFormDataModel model, FormDef f) {
		f.setAnswer(wrapData(result), ref);		
	}
		
	public boolean canCascade () {
		return true;
	}
	
	public boolean equals (Object o) {
		if (o instanceof Recalculate) {
			Recalculate r = (Recalculate)o;
			if (this == r)
				return true;
			
			return super.equals(r);
		} else {
			return false;
		}			
	}
	
	private static IAnswerData wrapData (Object val) {
		if (val instanceof Boolean) {
			return new IntegerData(((Boolean)val).booleanValue() ? 1 : 0);
		} else if (val instanceof Double) {
			double d = ((Double)val).doubleValue();
			return Double.isNaN(d) ? null : new DecimalData(d);
		} else if (val instanceof String) {
			String s = (String)val;
			return s.length() > 0 ? new StringData(s) : null;
		} else if (val instanceof Date) {
			return new DateData((Date)val);
		} else {
			throw new RuntimeException("unrecognized data type in 'calculate' expression");
		}
	}
}