package org.javarosa.core.model;

import kotlin.Pair;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.instance.DataInstance;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xpath.expr.XPathCmpExpr;
import org.javarosa.xpath.expr.XPathEqExpr;
import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.javarosa.xpath.expr.XPathNumericLiteral;
import org.javarosa.xpath.expr.XPathPathExpr;
import org.javarosa.xpath.expr.XPathStringLiteral;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class CompareChildToAbsoluteExpression {

    private final XPathPathExpr relativeSide;
    private final XPathExpression absoluteSide;
    private final XPathExpression original;

    public CompareChildToAbsoluteExpression(XPathPathExpr relativeSide, XPathExpression absoluteSide, XPathExpression original) {
        this.relativeSide = relativeSide;
        this.absoluteSide = absoluteSide;
        this.original = original;
    }

    public Object evalRelative(DataInstance sourceInstance, EvaluationContext evaluationContext, TreeReference child, int childIndex) {
        EvaluationContext rescopedContext = evaluationContext.rescope(child, childIndex);
        return getRelativeSide().eval(sourceInstance, rescopedContext).unpack();
    }

    public Object evalAbsolute(DataInstance sourceInstance, EvaluationContext evaluationContext) {
        if (absoluteSide instanceof XPathPathExpr) {
            return ((XPathPathExpr) getAbsoluteSide()).eval(sourceInstance, evaluationContext).unpack();
        } else {
            return absoluteSide.eval(sourceInstance, evaluationContext);
        }
    }

    public XPathPathExpr getRelativeSide() {
        return relativeSide;
    }

    public XPathExpression getAbsoluteSide() {
        return absoluteSide;
    }

    public XPathExpression getOriginal() {
        return original;
    }

    @Nullable
    public static CompareChildToAbsoluteExpression parse(XPathExpression expression) {
        XPathExpression a = null;
        XPathExpression b = null;

        if (expression instanceof XPathCmpExpr) {
            a = ((XPathCmpExpr) expression).a;
            b = ((XPathCmpExpr) expression).b;
        } else if (expression instanceof XPathEqExpr) {
            a = ((XPathEqExpr) expression).a;
            b = ((XPathEqExpr) expression).b;
        } else if (expression instanceof XPathFuncExpr && expression.isIdempotent()
            && ((XPathFuncExpr) expression).args.length == 2) {
            a = ((XPathFuncExpr) expression).args[0];
            b = ((XPathFuncExpr) expression).args[1];
        }

        Pair<XPathPathExpr, XPathExpression> relativeAndAbsolute = getRelativeAndAbsolute(a, b);
        if (relativeAndAbsolute != null) {
            return new CompareChildToAbsoluteExpression(relativeAndAbsolute.getFirst(), relativeAndAbsolute.getSecond(), expression);
        } else {
            return null;
        }
    }

    private static Pair<XPathPathExpr, XPathExpression> getRelativeAndAbsolute(XPathExpression a, XPathExpression b) {
        XPathPathExpr relative = null;
        XPathExpression absolute = null;

        Queue<XPathExpression> subExpressions = new LinkedList<>(Arrays.asList(a, b));
        while (!subExpressions.isEmpty()) {
            XPathExpression subExpression = subExpressions.poll();
            if (subExpression instanceof XPathPathExpr && ((XPathPathExpr) subExpression).init_context == XPathPathExpr.INIT_CONTEXT_RELATIVE)
                relative = (XPathPathExpr) subExpression;
            else if (subExpression instanceof XPathPathExpr && ((XPathPathExpr) subExpression).init_context == XPathPathExpr.INIT_CONTEXT_ROOT) {
                absolute = subExpression;
            } else if (subExpression instanceof XPathNumericLiteral || subExpression instanceof XPathStringLiteral) {
                absolute = subExpression;
            }
        }

        if (relative != null && absolute != null) {
            return new Pair<>(relative, absolute);
        } else {
            return null;
        }
    }
}
