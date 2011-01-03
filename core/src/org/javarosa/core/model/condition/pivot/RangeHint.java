/**
 * 
 */
package org.javarosa.core.model.condition.pivot;

import java.util.Vector;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.IConditionExpr;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.xpath.expr.XPathFuncExpr;

/**
 * @author ctsims
 *
 */
public abstract class RangeHint<T extends IAnswerData> implements ConstraintHint{
	
	Double min;
	Double max;
	boolean minInclusive;
	boolean maxInclusive;
	
	public void init(EvaluationContext c, IConditionExpr conditional, FormInstance instance) throws UnpivotableExpressionException {
		
		Vector<Object> pivots = conditional.pivot(instance, c);
		
		Vector<CmpPivot> internalPivots = new Vector<CmpPivot>();
		for(Object p : pivots) {
			if(!(p instanceof CmpPivot)) {
				throw new UnpivotableExpressionException();
			}
			internalPivots.addElement((CmpPivot)p);
		}
		
		if(internalPivots.size() > 2) {
			//For now.
			throw new UnpivotableExpressionException();
		}
		
		for(CmpPivot pivot : internalPivots) {
			evaluatePivot(pivot, conditional, c, instance);
		}
	}
	
	public T getMin() {
		return min == null ? null : castToValue(min);
	}
	
	public boolean isMinInclusive() {
		return minInclusive;
	}
	
	public T getMax() {
		return max == null ? null : castToValue(max);
	}
	
	public boolean isMaxInclusive() {
		return maxInclusive;
	}
	
	private void evaluatePivot(CmpPivot pivot, IConditionExpr conditional, EvaluationContext c, FormInstance instance) throws UnpivotableExpressionException {
		double unit = unit();
		double val = pivot.getVal();
		double lt = val - unit;
		double gt = val + unit;
		
		c.isConstraint = true;
		
		c.candidateValue = castToValue(val);
		boolean eq = XPathFuncExpr.toBoolean(conditional.eval(instance, c)).booleanValue();
		
		c.candidateValue = castToValue(lt);
		boolean ltr = XPathFuncExpr.toBoolean(conditional.eval(instance, c)).booleanValue();
		
		c.candidateValue = castToValue(gt);
		boolean gtr = XPathFuncExpr.toBoolean(conditional.eval(instance, c)).booleanValue();
		
		if(ltr && !gtr) {
			max = new Double(val);
			maxInclusive = eq;
		}
		
		if(!ltr && gtr) {
			min = new Double(val);
			minInclusive = eq;
		}
	}
	
	protected abstract T castToValue(double value);
	
	protected abstract double unit();
}
