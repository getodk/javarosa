package org.javarosa.xpath.expr;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class XPathFuncExprTest {

    @Test
    public void isIdempotent_whenArgsContainsNonIdempotentFunc_returnsFalse() {
        // string(random())
        XPathFuncExpr expr = new XPathFuncExpr(new XPathQName("string"), new XPathExpression[] {
            new XPathFuncExpr(new XPathQName("random"))
        });

        assertThat(expr.isIdempotent(), equalTo(false));
    }

    @Test
    public void containsFunc_whenFunctionNameMatches_returnTrue() {
        // random()
        XPathFuncExpr expr = new XPathFuncExpr(new XPathQName("random"));
        assertThat(expr.containsFunc("random"), equalTo(true));
    }

    @Test
    public void containsFunc_whenArgsIncludeFunction_returnTrue() {
        // string(random())
        XPathFuncExpr expr = new XPathFuncExpr(new XPathQName("string"), new XPathExpression[] {
            new XPathFuncExpr(new XPathQName("random"))
        });

        assertThat(expr.containsFunc("random"), equalTo(true));
    }

    @Test
    public void containFunc_whenFunctionNameDoesNotMatch_returnsFalse() {
        // random()
        XPathFuncExpr expr = new XPathFuncExpr(new XPathQName("random"));
        assertThat(expr.containsFunc("other"), equalTo(false));
    }
}