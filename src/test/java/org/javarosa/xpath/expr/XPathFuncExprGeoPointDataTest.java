package org.javarosa.xpath.expr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.javarosa.core.model.data.GeoPointData;
import org.junit.Test;

public class XPathFuncExprGeoPointDataTest {

    @Test
    public void toNumeric_withGeoPointData_returnsAccuracy() {
        GeoPointData data =
            new GeoPointData(new double[] {1.22, 2.33, 3.33, 0.01});

        assertEquals(0.01, XPathFuncExpr.toNumeric(data), 0);
    }

    @Test
    public void toNumeric_withGeoPointDataWithoutAccuracy_returnsNoAccuracyValue() {
        GeoPointData data =
            new GeoPointData(new double[] {0.0, 0.0, 0.0});

        assertEquals(9999999.0, XPathFuncExpr.toNumeric(data), 0);
    }

    @Test
    public void toString_withGeoPointData_returnsSpaceSeparatedValues() {
        GeoPointData data =
            new GeoPointData(new double[] {1.22, 2.33, 3.33, 0.01});

        assertEquals("1.22 2.33 3.33 0.01", XPathFuncExpr.toString(data));
    }

    @Test
    public void toBoolean_withNonZeroGeoPointData_returnsTrue() {
        GeoPointData data =
            new GeoPointData(new double[] {1.22, 2.33, 3.33, 0.0});

        assertTrue(XPathFuncExpr.toBoolean(data));
    }

    @Test
    public void toBoolean_withZeroGeoPointData_returnsFalse() {
        GeoPointData data =
            new GeoPointData(new double[] {0.0, 0.0, 0.0, 0.0});

        assertFalse(XPathFuncExpr.toBoolean(data));
    }
}