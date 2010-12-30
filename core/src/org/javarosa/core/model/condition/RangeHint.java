/**
 * 
 */
package org.javarosa.core.model.condition;

import java.util.Vector;

import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.xpath.expr.XPathExpression;

/**
 * @author ctsims
 *
 */
public class RangeHint {
	
	public RangeHint(EvaluationContext c, XPathExpression conditional, FormInstance instance)   {
		Vector<Pivot> pivots = conditional.eval(instance,c);
		
	}
}
