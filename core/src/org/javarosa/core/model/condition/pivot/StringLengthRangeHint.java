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

	protected StringData castToValue(double value) {
		String placeholder = "";
		for(int i = 0 ; i < ((int)value) ; ++i) {
			placeholder += "X";
		}
		return new StringData(placeholder);
	}

	protected double unit() {
		return 1;
	}

}
