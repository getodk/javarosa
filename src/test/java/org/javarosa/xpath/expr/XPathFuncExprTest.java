package org.javarosa.xpath.expr;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.javarosa.core.model.utils.DateFormatter.xpathPatternAsJavaTimePattern;

public class XPathFuncExprTest {

    @Test //TODO xpathPatternAsJavaTimePattern() needs more testing
    public void XPathExprDateFormatsAreHandled() {
        //testEval("format-date('2018-01-02T10:20:30.123', \"%Y-%m-%e %H:%M:%S\")", "2018-01-2 10:20:30");
        //testEval("date-time('2000-01-01T10:20:30.000')", DateUtils.parseDateTime("2000-01-01T10:20:30.000"));
        assertEquals("Y-MM-dd'T'HH:m:ss.SSS", xpathPatternAsJavaTimePattern("%Y-%m-%dT%H:%M:%S.%3"));
        assertEquals("Y-MM-d HH:m:ss", xpathPatternAsJavaTimePattern("%Y-%m-%e %H:%M:%S"));

        //from original DateFormats
        //TIMESTAMP_HTTP(9, " ", "%a, %d %b %Y", "%H:%M:%S GMT")
        //HUMAN_READABLE_SHORT(2, " ", "%d/%m/YY", "HH:mm")
        //TIMESTAMP_SUFFIX(7, "", "%Y%m%d", "HHmmss")
        //TODO - fix this... assertEquals("EEE, dd MMM Y HH:m:ss Gm'T'", xpathPatternAsJavaTimePattern("%a, %d %b %Y %H:%M:%S GMT"));
        assertEquals("EEE, dd MMM Y HH:m:ss", xpathPatternAsJavaTimePattern("%a, %d %b %Y %H:%M:%S"));
        assertEquals("dd/MM/YY HH:m", xpathPatternAsJavaTimePattern("%d/%m/YY %H:%M"));
        assertEquals("YMMddHHmss", xpathPatternAsJavaTimePattern("%Y%m%d%H%M%S"));
    }

    @Test
    public void isIdempotent_whenArgsContainsNonIdempotentFunc_returnsFalse() {
        // string(random())
        XPathFuncExpr expr = new XPathFuncExpr(new XPathQName("string"), new XPathExpression[]{
                new XPathFuncExpr(new XPathQName("random"), new XPathExpression[]{})
        });

        assertThat(expr.isIdempotent(), equalTo(false));
    }
}