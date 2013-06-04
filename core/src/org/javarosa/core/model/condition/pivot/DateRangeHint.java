/**
 *
 */
package org.javarosa.core.model.condition.pivot;

import java.util.Date;

import org.javarosa.core.model.data.DateData;
import org.javarosa.xpath.expr.XPathFuncExpr;

/**
 * @author ctsims
 *
 */
public class DateRangeHint extends RangeHint<DateData> {

	protected DateData castToValue(double value) throws UnpivotableExpressionException {
		return new DateData((Date)XPathFuncExpr.toDate(new Double(Math.floor(value)), false));
	}

	protected double unit() {
		return 1;
	}

}
