package org.javarosa.core.model;

import kotlin.Pair;
import org.javarosa.xpath.expr.XPathCmpExpr;
import org.javarosa.xpath.expr.XPathEqExpr;
import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.expr.XPathPathExpr;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

class CompareChildToAbsoluteExpression {

    private final XPathPathExpr relativeSide;
    private final XPathPathExpr absoluteSide;
    private final XPathExpression original;

    public CompareChildToAbsoluteExpression(XPathPathExpr relativeSide, XPathPathExpr absoluteSide, XPathExpression original) {
        this.relativeSide = relativeSide;
        this.absoluteSide = absoluteSide;
        this.original = original;
    }

    public XPathPathExpr getRelativeSide() {
        return relativeSide;
    }

    public XPathPathExpr getAbsoluteSide() {
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
        }

        Pair<XPathPathExpr, XPathPathExpr> relativeAndAbsolute = getRelativeAndAbsolute(a, b);
        if (relativeAndAbsolute != null) {
            return new CompareChildToAbsoluteExpression(relativeAndAbsolute.getFirst(), relativeAndAbsolute.getSecond(), expression);
        } else {
            return null;
        }
    }

    private static Pair<XPathPathExpr, XPathPathExpr> getRelativeAndAbsolute(XPathExpression a, XPathExpression b) {
        XPathPathExpr relative = null;
        XPathPathExpr absolute = null;

        Queue<XPathExpression> subExpressions = new LinkedList<>(Arrays.asList(a, b));
        while (!subExpressions.isEmpty()) {
            XPathExpression subExpression = subExpressions.poll();
            if (subExpression instanceof XPathPathExpr && ((XPathPathExpr) subExpression).init_context == XPathPathExpr.INIT_CONTEXT_RELATIVE) {
                relative = (XPathPathExpr) subExpression;
            } else if (subExpression instanceof XPathPathExpr && ((XPathPathExpr) subExpression).init_context == XPathPathExpr.INIT_CONTEXT_ROOT) {
                absolute = (XPathPathExpr) subExpression;
            }
        }

        if (relative != null && absolute != null) {
            return new Pair<>(relative, absolute);
        } else {
            return null;
        }
    }
}
