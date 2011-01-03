/**
 * 
 */
package org.javarosa.core.model.condition.pivot;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.xpath.expr.XPathExpression;

/**
 * @author ctsims
 *
 */
public interface ConstraintHint {
	
	public void init(EvaluationContext c, XPathExpression conditional, FormInstance instance) throws UnpivotableExpressionException;
}
