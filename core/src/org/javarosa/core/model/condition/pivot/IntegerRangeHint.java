/**
 * 
 */
package org.javarosa.core.model.condition.pivot;

import org.javarosa.core.model.data.IntegerData;

/**
 * @author ctsims
 *
 */
public class IntegerRangeHint extends RangeHint<IntegerData> {

	protected IntegerData castToValue(double value) {
		return new IntegerData((int)Math.floor(value));
	}

	protected double unit() {
		return 1;
	}

}
