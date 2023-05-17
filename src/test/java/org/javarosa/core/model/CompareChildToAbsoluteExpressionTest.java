package org.javarosa.core.model;

import org.javarosa.xpath.expr.XPathEqExpr;
import org.javarosa.xpath.expr.XPathNumericLiteral;
import org.javarosa.xpath.expr.XPathPathExpr;
import org.javarosa.xpath.expr.XPathQName;
import org.javarosa.xpath.expr.XPathStep;
import org.javarosa.xpath.expr.XPathStringLiteral;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public class CompareChildToAbsoluteExpressionTest {

    @Test
    public void parse_parsesStringLiteralAsAbsolute() {
        XPathEqExpr expression = new XPathEqExpr(
            true,
            new XPathPathExpr(XPathPathExpr.INIT_CONTEXT_RELATIVE, new XPathStep[]{
                new XPathStep(XPathStep.AXIS_CHILD, new XPathQName("name")) }
            ),
            new XPathStringLiteral("string")
        );

        CompareChildToAbsoluteExpression parsed = CompareChildToAbsoluteExpression.parse(expression);
        assertThat(parsed, not(nullValue()));
        assertThat(parsed.getRelativeSide(), equalTo(expression.a));
        assertThat(parsed.getAbsoluteSide(), equalTo(expression.b));
    }

    @Test
    public void parse_parsesNumericLiteralAsAbsolute() {
        XPathEqExpr expression = new XPathEqExpr(
            true,
            new XPathPathExpr(XPathPathExpr.INIT_CONTEXT_RELATIVE, new XPathStep[]{
                new XPathStep(XPathStep.AXIS_CHILD, new XPathQName("name")) }
            ),
            new XPathNumericLiteral(45.0)
        );

        CompareChildToAbsoluteExpression parsed = CompareChildToAbsoluteExpression.parse(expression);
        assertThat(parsed, not(nullValue()));
        assertThat(parsed.getRelativeSide(), equalTo(expression.a));
        assertThat(parsed.getAbsoluteSide(), equalTo(expression.b));
    }

    @Test
    public void parse_parsesRelativeAndAbsoluteRegardlessOfSide() {
        XPathPathExpr relative = new XPathPathExpr(XPathPathExpr.INIT_CONTEXT_RELATIVE, new XPathStep[]{
            new XPathStep(XPathStep.AXIS_CHILD, new XPathQName("name"))}
        );
        XPathPathExpr absolute = new XPathPathExpr(XPathPathExpr.INIT_CONTEXT_ROOT, new XPathStep[]{
            new XPathStep(XPathStep.AXIS_CHILD, new XPathQName("something"))}
        );

        XPathEqExpr ltr = new XPathEqExpr(true, relative, absolute);
        XPathEqExpr rtl = new XPathEqExpr(true, absolute, relative);
        CompareChildToAbsoluteExpression ltrParsed = CompareChildToAbsoluteExpression.parse(ltr);
        CompareChildToAbsoluteExpression rtlParsed = CompareChildToAbsoluteExpression.parse(rtl);

        assertThat(ltrParsed, not(nullValue()));
        assertThat(ltrParsed.getRelativeSide(), equalTo(relative));
        assertThat(ltrParsed.getAbsoluteSide(), equalTo(absolute));

        assertThat(rtlParsed, not(nullValue()));
        assertThat(rtlParsed.getRelativeSide(), equalTo(relative));
        assertThat(rtlParsed.getAbsoluteSide(), equalTo(absolute));
    }
}