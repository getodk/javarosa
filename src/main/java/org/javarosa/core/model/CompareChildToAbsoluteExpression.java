package org.javarosa.core.model;

import org.javarosa.xpath.expr.XPathCmpExpr;
import org.javarosa.xpath.expr.XPathEqExpr;
import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.expr.XPathPathExpr;
import org.jetbrains.annotations.Nullable;

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
        XPathPathExpr left = null;
        XPathPathExpr right = null;

        if (expression instanceof XPathCmpExpr &&
            ((XPathCmpExpr) expression).a instanceof XPathPathExpr &&
            ((XPathCmpExpr) expression).b instanceof XPathPathExpr) {

            left = (XPathPathExpr) ((XPathCmpExpr) expression).a;
            right = (XPathPathExpr) ((XPathCmpExpr) expression).b;
        } else if (expression instanceof XPathEqExpr &&
            ((XPathEqExpr) expression).a instanceof XPathPathExpr &&
            ((XPathEqExpr) expression).b instanceof XPathPathExpr) {
            left = (XPathPathExpr) ((XPathEqExpr) expression).a;
            right = (XPathPathExpr) ((XPathEqExpr) expression).b;
        }

        if (left != null && left.init_context == XPathPathExpr.INIT_CONTEXT_RELATIVE &&
            right.init_context == XPathPathExpr.INIT_CONTEXT_ROOT) {
            return new CompareChildToAbsoluteExpression(left, right, expression);
        } else {
            return null;
        }
    }
}
