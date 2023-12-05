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

/**
 * Convenience class for identifying and dealing with expressions that compare a child node to something else in the
 * form like:
 * <p/>
 * name = /data/search
 * <p/>
 * In the example above, "name" would be the "node side" (a relative expression) and "/data/search" would be the
 * "context side" (an absolute or context expression). These expressions are useful to be able to identify for caching
 * evaluations as nodes with the same node side evaluation and context side expression will ultimately evaluate the same
 * result.
 * <p/>
 * This class does not support expressions comparing two relative expressions as there would be two nodes sides.
 */
public class CompareToNodeExpression {

    private final XPathPathExpr nodeSide;
    private final XPathExpression contextSide;
    private final XPathExpression original;

    public CompareToNodeExpression(XPathPathExpr nodeSide, XPathExpression contextSide, XPathExpression original) {
        this.nodeSide = nodeSide;
        this.contextSide = contextSide;
        this.original = original;
    }

    public Object evalNodeSide(DataInstance sourceInstance, EvaluationContext evaluationContext, TreeReference child, int childIndex) {
        EvaluationContext rescopedContext = evaluationContext.rescope(child, childIndex);
        return getNodeSide().eval(sourceInstance, rescopedContext).unpack();
    }

    public Object evalContextSide(DataInstance sourceInstance, EvaluationContext evaluationContext) {
        if (contextSide instanceof XPathPathExpr) {
            return ((XPathPathExpr) getContextSide()).eval(sourceInstance, evaluationContext).unpack();
        } else {
            return contextSide.eval(sourceInstance, evaluationContext);
        }
    }

    public XPathPathExpr getNodeSide() {
        return nodeSide;
    }

    public XPathExpression getContextSide() {
        return contextSide;
    }

    public XPathExpression getOriginal() {
        return original;
    }

    @Nullable
    public static CompareToNodeExpression parse(XPathExpression expression) {
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

        Pair<XPathPathExpr, XPathExpression> nodeAndContextSides = getNodeAndContextSides(a, b);
        if (nodeAndContextSides != null) {
            return new CompareToNodeExpression(nodeAndContextSides.getFirst(), nodeAndContextSides.getSecond(), expression);
        } else {
            return null;
        }
    }

    private static Pair<XPathPathExpr, XPathExpression> getNodeAndContextSides(XPathExpression a, XPathExpression b) {
        XPathPathExpr node = null;
        XPathExpression context = null;

        Queue<XPathExpression> subExpressions = new LinkedList<>(Arrays.asList(a, b));
        while (!subExpressions.isEmpty()) {
            XPathExpression subExpression = subExpressions.poll();
            if (subExpression instanceof XPathPathExpr) {
                if (((XPathPathExpr) subExpression).init_context == XPathPathExpr.INIT_CONTEXT_RELATIVE)
                    node = (XPathPathExpr) subExpression;
                else {
                    context = subExpression;
                }
            } else if (subExpression instanceof XPathNumericLiteral || subExpression instanceof XPathStringLiteral) {
                context = subExpression;
            }
        }

        if (node != null && context != null) {
            return new Pair<>(node, context);
        } else {
            return null;
        }
    }
}
