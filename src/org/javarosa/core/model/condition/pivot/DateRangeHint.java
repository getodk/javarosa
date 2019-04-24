/**
 *
 */
package org.javarosa.core.model.condition.pivot;

import org.javarosa.core.model.data.DateData;
import org.javarosa.xpath.expr.XPathFuncExpr;

import java.util.Date;

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
