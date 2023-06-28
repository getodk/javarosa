package org.javarosa.xpath.expr;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class XPathFilterExprTest {

    @Test
    public void containsFunc_whenFunctionNotInExpressionOrPredicates_returnsFalse() {
        // a()[b()]
        XPathFilterExpr expr = new XPathFilterExpr(
            new XPathFuncExpr(new XPathQName("a")),
            new XPathExpression[]{new XPathFuncExpr(new XPathQName("b"))}
        );

        assertThat(expr.containsFunc("c"), equalTo(false));
    }

    @Test
    public void containsFunc_whenFunctionInExpression_returnsTrue() {
        // a()[b()]
        XPathFilterExpr expr = new XPathFilterExpr(
            new XPathFuncExpr(new XPathQName("a")),
            new XPathExpression[]{new XPathFuncExpr(new XPathQName("b"))}
        );

        assertThat(expr.containsFunc("a"), equalTo(true));
    }

    @Test
    public void containsFunc_whenFunctionInPredicates_returnsTrue() {
        // a()[b()]
        XPathFilterExpr expr = new XPathFilterExpr(
            new XPathFuncExpr(new XPathQName("a")),
            new XPathExpression[]{new XPathFuncExpr(new XPathQName("b"))}
        );

        assertThat(expr.containsFunc("b"), equalTo(true));
    }
}