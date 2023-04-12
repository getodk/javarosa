package org.javarosa.core.model;

import org.javarosa.core.model.condition.PredicateCache;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xpath.expr.XPathExpression;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 *  In memory implementation of a {@link PredicateCache}. Cannot cache predicate evaluations that contain a
 *  non-idempotent function.
 */
public class IdempotentInMemPredicateCache implements PredicateCache {

    public Map<String, List<TreeReference>> cachedEvaluations = new HashMap<>();

    @Override
    @NotNull
    public List<TreeReference> get(TreeReference nodeSet, XPathExpression predicate, Supplier<List<TreeReference>> onMiss) {
        String key = getKey(nodeSet, predicate);

        if (cachedEvaluations.containsKey(key)) {
            return cachedEvaluations.get(key);
        } else {
            List<TreeReference> references = onMiss.get();
            if (isCacheable(predicate)) {
                cachedEvaluations.put(key, references);
            }

            return references;
        }
    }

    private String getKey(TreeReference nodeSet, XPathExpression predicate) {
        return nodeSet.toString() + predicate.toString();
    }

    private boolean isCacheable(XPathExpression predicate) {
        return predicate.isIdempotent();
    }
}
