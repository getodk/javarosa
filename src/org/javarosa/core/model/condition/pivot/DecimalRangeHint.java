/**
 * 
 */
package org.javarosa.core.model.condition.pivot;

import org.javarosa.core.model.data.DecimalData;

/**
 * @author ctsims
 *
 */
public class DecimalRangeHint extends RangeHint<DecimalData> {

	protected DecimalData castToValue(double value) throws UnpivotableExpressionException {
		return new DecimalData(value);
	}

	protected double unit() {
		return Double.MIN_VALUE;
	}

}
