/**
 * 
 */
package org.javarosa.core.model.condition.pivot;

import java.util.Date;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.expr.XPathFuncExpr;

/**
 * @author ctsims
 *
 */
public class DateRangeHint extends RangeHint<DateData> {

	protected DateData castToValue(double value) {
		return new DateData((Date)XPathFuncExpr.toDate(((int)Math.round(value))));
	}

	protected double unit() {
		return 1;
	}

}
