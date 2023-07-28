package org.javarosa.xpath.expr;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class XPathPathExprTest {

    @Test
    public void containsFunc_whenNoFilter_andNoStepPredicatesContainFunction_returnsFalse() {
        XPathStep step = new XPathStep();
        step.predicates = new XPathExpression[]{new XPathFuncExpr(new XPathQName("a"))};

        XPathPathExpr expr = new XPathPathExpr(
            XPathPathExpr.INIT_CONTEXT_ROOT,
            new XPathStep[]{step}
        );

        assertThat(expr.containsFunc("b"), equalTo(false));
    }

    @Test
    public void containsFunc_whenNeitherFilterOrStepPredicatesContainFunction_returnsFalse() {
        XPathStep step = new XPathStep();
        step.predicates = new XPathExpression[]{new XPathFuncExpr(new XPathQName("b"))};

        XPathPathExpr expr = new XPathPathExpr(
            new XPathFilterExpr(new XPathFuncExpr(new XPathQName("a")), new XPathExpression[]{}),
            new XPathStep[]{step}
        );

        assertThat(expr.containsFunc("c"), equalTo(false));
    }

    @Test
    public void containsFunc_whenFunctionInFilter_returnsTrue() {
        XPathPathExpr expr = new XPathPathExpr(
            new XPathFilterExpr(new XPathFuncExpr(new XPathQName("a")), new XPathExpression[]{}),
            new XPathStep[]{}
        );

        assertThat(expr.containsFunc("a"), equalTo(true));
    }

    @Test
    public void containsFunc_whenFunctionInStep_returnsTrue() {
        XPathStep step = new XPathStep();
        step.predicates = new XPathExpression[]{new XPathFuncExpr(new XPathQName("a"))};

        XPathPathExpr expr = new XPathPathExpr(
            XPathPathExpr.INIT_CONTEXT_ROOT,
            new XPathStep[]{step}
        );

        assertThat(expr.containsFunc("a"), equalTo(true));
    }
}