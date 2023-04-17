package org.javarosa.core.model;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.PredicateFilter;
import org.javarosa.core.model.instance.DataInstance;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xpath.expr.XPathCmpExpr;
import org.javarosa.xpath.expr.XPathEqExpr;
import org.javarosa.xpath.expr.XPathExpression;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Caches down stream evaluations (in the {@link PredicateFilter} chain) for supported expressions - currently just
 * {@link XPathCmpExpr} and {@link XPathEqExpr}. Repeated evaluations are fetched in O(1) time.
 */
public class CachingPredicateFilter implements PredicateFilter {

    private final Map<String, List<TreeReference>> cachedEvaluations = new HashMap<>();

    @Nullable
    @Override
    public List<TreeReference> filter(DataInstance sourceInstance, TreeReference nodeSet, XPathExpression predicate, List<TreeReference> children, EvaluationContext evaluationContext, Supplier<List<TreeReference>> next) {
        CompareChildToAbsoluteExpression candidate = CompareChildToAbsoluteExpression.parse(predicate);

        if (candidate != null) {
            Object absoluteValue = CompareChildToAbsoluteExpression.evalAbsolute(sourceInstance, evaluationContext, candidate);
            String key = nodeSet.toString() + predicate + candidate.getRelativeSide() + absoluteValue.toString();

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
    }

}
