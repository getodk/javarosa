/**
 * 
 */
package org.javarosa.core.model.condition.pivot;

import org.javarosa.core.model.data.StringData;

/**
 * @author ctsims
 *
 */
public class StringLengthRangeHint extends RangeHint<StringData> {

	protected StringData castToValue(double value) throws UnpivotableExpressionException {
		StringBuilder sb = new StringBuilder();
		for(int i = 0 ; i < ((int)value) ; ++i) {
			sb.append("X");
		}
		return new StringData(sb.toString());
	}

	protected double unit() {
		return 1;
	}

}
