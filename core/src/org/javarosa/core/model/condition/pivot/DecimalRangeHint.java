/**
 * 
 */
package org.javarosa.core.model.condition.pivot;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.xpath.expr.XPathExpression;

/**
 * @author ctsims
 *
 */
public class DecimalRangeHint extends RangeHint<DecimalData> {

	protected DecimalData castToValue(double value) {
		return new DecimalData(value);
	}

	protected double unit() {
		return Double.MIN_VALUE;
	}

}
