package org.javarosa.core.model;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.PredicateFilter;
import org.javarosa.core.model.instance.DataInstance;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xpath.expr.XPathExpression;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Caches down stream evaluations (in the {@link PredicateFilter} chain) for "idempotent" (with respect to current form
 * state) predicates. Can only be used for static instances or in cases where form state won't change - will cause
 * clashes otherwise. Repeated evaluations are fetched in O(1) time.
 */
public class IdempotentPredicateCache implements PredicateFilter {

    private final Map<String, List<TreeReference>> cachedEvaluations = new HashMap<>();

    @NotNull
    @Override
    public List<TreeReference> filter(@NotNull DataInstance sourceInstance, @NotNull TreeReference nodeSet, @NotNull XPathExpression predicate, @NotNull List<TreeReference> children, @NotNull EvaluationContext evaluationContext, @NotNull Supplier<List<TreeReference>> next) {
        String key = getKey(nodeSet, predicate);

        if (cachedEvaluations.containsKey(key)) {
            return cachedEvaluations.get(key);
        } else {
            List<TreeReference> filtered = next.get();
            if (isCacheable(predicate)) {
                cachedEvaluations.put(key, filtered);
            }

            return filtered;
        }
    }

    private String getKey(TreeReference nodeSet, XPathExpression predicate) {
        return nodeSet.toString() + predicate.toString();
    }

    private boolean isCacheable(XPathExpression predicate) {
        return predicate.isIdempotent();
    }
}
