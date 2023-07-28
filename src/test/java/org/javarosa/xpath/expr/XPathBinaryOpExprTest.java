package org.javarosa.xpath.expr;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.instance.DataInstance;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class XPathBinaryOpExprTest {

    @Test
    public void containsFunc_whenNeitherSideContainsMatchingFunction_returnsFalse() {
        FakeXPathBinaryOpExpr expr = new FakeXPathBinaryOpExpr(
            new XPathFuncExpr(new XPathQName("a")),
            new XPathFuncExpr(new XPathQName("b"))
        );

        assertThat(expr.containsFunc("c"), equalTo(false));
    }

    @Test
    public void containsFunc_whenEitherSideContainsMatchingFunction_returnTrue() {
        FakeXPathBinaryOpExpr expr = new FakeXPathBinaryOpExpr(
            new XPathFuncExpr(new XPathQName("a")),
            new XPathFuncExpr(new XPathQName("b"))
        );

        assertThat(expr.containsFunc("a"), equalTo(true));
        assertThat(expr.containsFunc("b"), equalTo(true));
    }

    private static class FakeXPathBinaryOpExpr extends XPathBinaryOpExpr {

        public FakeXPathBinaryOpExpr(XPathExpression a, XPathExpression b) {
            super(a, b);
        }

        @Override
        public Object eval(DataInstance model, EvaluationContext evalContext) {
            return null;
        }

        @Override
        public boolean isIdempotent() {
            return false;
        }
    }
}