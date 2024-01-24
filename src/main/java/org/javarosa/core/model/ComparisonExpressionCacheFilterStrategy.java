package org.javarosa.core.model;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.FilterStrategy;
import org.javarosa.core.model.instance.DataInstance;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xpath.expr.XPathBoolExpr;
import org.javarosa.xpath.expr.XPathCmpExpr;
import org.javarosa.xpath.expr.XPathEqExpr;
import org.javarosa.xpath.expr.XPathExpression;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Caches down stream evaluations (in the {@link FilterStrategy} chain) for supported expressions - currently just
 * {@link XPathCmpExpr} and {@link XPathEqExpr}. Repeated evaluations are fetched in O(1) time.
 */
public class ComparisonExpressionCacheFilterStrategy implements FilterStrategy {

    private final Map<String, List<TreeReference>> cachedEvaluations = new HashMap<>();

    @NotNull
    @Override
    public List<TreeReference> filter(@NotNull DataInstance sourceInstance, @NotNull TreeReference nodeSet, @NotNull XPathExpression predicate, @NotNull List<TreeReference> children, @NotNull EvaluationContext evaluationContext, @NotNull Supplier<List<TreeReference>> next) {
        if (sourceInstance.getInstanceId() == null) {
            return next.get();
        }

        CompareToNodeExpression candidate = CompareToNodeExpression.parse(predicate);
        if (candidate != null) {
            String key = getKey(sourceInstance, nodeSet, predicate, evaluationContext, candidate);

            if (cachedEvaluations.containsKey(key)) {
                return cachedEvaluations.get(key);
            } else {
                List<TreeReference> filtered = next.get();
                cachedEvaluations.put(key, filtered);
                return filtered;
            }
        } else if (predicate instanceof XPathBoolExpr) {
            XPathExpression a = ((XPathBoolExpr) predicate).a;
            XPathExpression b = ((XPathBoolExpr) predicate).b;

            CompareToNodeExpression candidateA = CompareToNodeExpression.parse(a);
            CompareToNodeExpression candidateB = CompareToNodeExpression.parse(b);

            if (candidateA != null && candidateB != null) {
                String keyA = getKey(sourceInstance, nodeSet, a, evaluationContext, candidateA);
                String keyB = getKey(sourceInstance, nodeSet, b, evaluationContext, candidateB);
                String key = keyA + keyB + predicate;

                if (cachedEvaluations.containsKey(key)) {
                    return cachedEvaluations.get(key);
                } else {
                    List<TreeReference> filtered = next.get();
                    cachedEvaluations.put(key, filtered);
                    return filtered;
                }
            } else {
                return next.get();
            }
        } else {
            return next.get();
        }
    }

    @NotNull
    private static String getKey(@NotNull DataInstance sourceInstance, @NotNull TreeReference nodeSet, @NotNull XPathExpression predicate, @NotNull EvaluationContext evaluationContext, CompareToNodeExpression candidate) {
        Object absoluteValue = candidate.evalContextSide(sourceInstance, evaluationContext);
        return nodeSet.toString() + predicate + candidate.getNodeSide() + absoluteValue.toString();
    }

}
