/**
 * 
 */
package org.javarosa.core.model.condition.pivot;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.IConditionExpr;
import org.javarosa.core.model.instance.FormInstance;

/**
 * @author ctsims
 *
 */
public interface ConstraintHint {
	
	public void init(EvaluationContext c, IConditionExpr conditional, FormInstance instance) throws UnpivotableExpressionException;
}
