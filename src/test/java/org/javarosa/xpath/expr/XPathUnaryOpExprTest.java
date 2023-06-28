package org.javarosa.xpath.expr;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.instance.DataInstance;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class XPathUnaryOpExprTest {

    @Test
    public void containsFunc_whenFunctionNotInExpression_returnsFalse() {
        // -a()
        FakeXPathUnaryOpExpr expr = new FakeXPathUnaryOpExpr(new XPathFuncExpr(new XPathQName("a")));
        assertThat(expr.containsFunc("b"), equalTo(false));
    }

    @Test
    public void containsFunc_whenFunctionInExpression_returnsTrue() {
        FakeXPathUnaryOpExpr expr = new FakeXPathUnaryOpExpr(new XPathFuncExpr(new XPathQName("a")));
        assertThat(expr.containsFunc("a"), equalTo(true));
    }

    private static class FakeXPathUnaryOpExpr extends XPathUnaryOpExpr {

        public FakeXPathUnaryOpExpr(XPathExpression a) {
            super(a);
        }

        @Override
        public Object eval(DataInstance model, EvaluationContext evalContext) {
            return null;
        }
    }
}