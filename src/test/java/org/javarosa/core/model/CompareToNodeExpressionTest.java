package org.javarosa.core.model;

import org.javarosa.xpath.expr.XPathEqExpr;
import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.expr.XPathFilterExpr;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.javarosa.xpath.expr.XPathNumericLiteral;
import org.javarosa.xpath.expr.XPathPathExpr;
import org.javarosa.xpath.expr.XPathQName;
import org.javarosa.xpath.expr.XPathStep;
import org.javarosa.xpath.expr.XPathStringLiteral;
import org.junit.Test;

import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public class CompareToNodeExpressionTest {

    @Test
    public void parse_doesNotParseExpressionsWhereBothSidesAreRelative() {
        XPathEqExpr expression = new XPathEqExpr(
            true,
            new XPathPathExpr(XPathPathExpr.INIT_CONTEXT_RELATIVE, new XPathStep[]{
                new XPathStep(XPathStep.AXIS_CHILD, new XPathQName("name")) }
            ),
            new XPathPathExpr(XPathPathExpr.INIT_CONTEXT_RELATIVE, new XPathStep[]{
                new XPathStep(XPathStep.AXIS_CHILD, new XPathQName("name")) }
            )
        );

        CompareToNodeExpression parsed = CompareToNodeExpression.parse(expression);
        assertThat(parsed, nullValue());
    }

    @Test
    public void parse_parsesStringLiteralAsContextSide() {
        XPathEqExpr expression = new XPathEqExpr(
            true,
            new XPathPathExpr(XPathPathExpr.INIT_CONTEXT_RELATIVE, new XPathStep[]{
                new XPathStep(XPathStep.AXIS_CHILD, new XPathQName("name")) }
            ),
            new XPathStringLiteral("string")
        );

        CompareToNodeExpression parsed = CompareToNodeExpression.parse(expression);
        assertThat(parsed, not(nullValue()));
        assertThat(parsed.getNodeSide(), equalTo(expression.a));
        assertThat(parsed.getContextSide(), equalTo(expression.b));
    }

    @Test
    public void parse_parsesNumericLiteralAsContextSide() {
        XPathEqExpr expression = new XPathEqExpr(
            true,
            new XPathPathExpr(XPathPathExpr.INIT_CONTEXT_RELATIVE, new XPathStep[]{
                new XPathStep(XPathStep.AXIS_CHILD, new XPathQName("name")) }
            ),
            new XPathNumericLiteral(45.0)
        );

        CompareToNodeExpression parsed = CompareToNodeExpression.parse(expression);
        assertThat(parsed, not(nullValue()));
        assertThat(parsed.getNodeSide(), equalTo(expression.a));
        assertThat(parsed.getContextSide(), equalTo(expression.b));
    }

    @Test
    public void parse_parsesIdempotentFunctionWithAbsoluteAndRelativeArgs() {
        String[] idempotentFunctions = XPathFuncExpr.IDEMPOTENT_FUNCTIONS;
        XPathFuncExpr expression = new XPathFuncExpr(
            new XPathQName(idempotentFunctions[new Random().nextInt(idempotentFunctions.length)]),
            new XPathExpression[]{
                new XPathPathExpr(XPathPathExpr.INIT_CONTEXT_RELATIVE, new XPathStep[]{
                    new XPathStep(XPathStep.AXIS_CHILD, new XPathQName("name")) }
                ),
                new XPathPathExpr(XPathPathExpr.INIT_CONTEXT_ROOT, new XPathStep[]{
                    new XPathStep(XPathStep.AXIS_CHILD, new XPathQName("something")) }
                ),
            }
        );

        CompareToNodeExpression parsed = CompareToNodeExpression.parse(expression);
        assertThat(parsed, not(nullValue()));
        assertThat(parsed.getNodeSide(), equalTo(expression.args[0]));
        assertThat(parsed.getContextSide(), equalTo(expression.args[1]));
    }

    @Test
    public void parse_doesNotParseNonIdempotentFunction() {
        XPathFuncExpr expression = new XPathFuncExpr(
            new XPathQName("blah"),
            new XPathExpression[]{
                new XPathPathExpr(XPathPathExpr.INIT_CONTEXT_RELATIVE, new XPathStep[]{
                    new XPathStep(XPathStep.AXIS_CHILD, new XPathQName("name")) }
                ),
                new XPathPathExpr(XPathPathExpr.INIT_CONTEXT_ROOT, new XPathStep[]{
                    new XPathStep(XPathStep.AXIS_CHILD, new XPathQName("something")) }
                ),
            }
        );

        CompareToNodeExpression parsed = CompareToNodeExpression.parse(expression);
        assertThat(parsed, nullValue());
    }

    @Test
    public void parse_parsesContextExpressionsAsContextSide() {
        XPathEqExpr expression = new XPathEqExpr(
            true,
            new XPathPathExpr(XPathPathExpr.INIT_CONTEXT_RELATIVE, new XPathStep[]{
                new XPathStep(XPathStep.AXIS_CHILD, new XPathQName("name")) }
            ),
            new XPathPathExpr(
                new XPathFilterExpr(new XPathFuncExpr(new XPathQName("blah"), new XPathExpression[0]), new XPathExpression[0]),
                new XPathStep[0]
            )
        );

        CompareToNodeExpression parsed = CompareToNodeExpression.parse(expression);
        assertThat(parsed, not(nullValue()));
        assertThat(parsed.getNodeSide(), equalTo(expression.a));
        assertThat(parsed.getContextSide(), equalTo(expression.b));
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
        CompareToNodeExpression ltrParsed = CompareToNodeExpression.parse(ltr);
        CompareToNodeExpression rtlParsed = CompareToNodeExpression.parse(rtl);

        assertThat(ltrParsed, not(nullValue()));
        assertThat(ltrParsed.getNodeSide(), equalTo(relative));
        assertThat(ltrParsed.getContextSide(), equalTo(absolute));

        assertThat(rtlParsed, not(nullValue()));
        assertThat(rtlParsed.getNodeSide(), equalTo(relative));
        assertThat(rtlParsed.getContextSide(), equalTo(absolute));
    }
}