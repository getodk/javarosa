package org.javarosa.xpath.expr;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class XPathFuncExprTest {

    @Test
    public void testSum() {
        String[] vals = {"12.1", "13.1"};
        Double out = XPathFuncExpr.sum(vals);
        assertEquals(25.2, out.doubleValue(), 0);
    }
    
    @Test
    public void testSumBadChars() {
        // The 12,000.1 will be ignored because sum will convert
        // the comma to a period resulting in 12.000.1 which is invalid
        String[] vals = {"12,000.1", "13.1"};
        Double out = XPathFuncExpr.sum(vals);
        assertEquals(13.1, out.doubleValue(), 0);
        
    }
}
