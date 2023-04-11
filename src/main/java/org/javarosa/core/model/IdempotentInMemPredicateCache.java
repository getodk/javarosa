package org.javarosa.core.model;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.PredicateCache;
import org.javarosa.core.model.condition.PredicateFilter;
import org.javarosa.core.model.instance.DataInstance;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xpath.expr.XPathExpression;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IdempotentInMemPredicateCache implements PredicateCache, PredicateFilter {

    public Map<String, List<TreeReference>> cachedEvaluations = new HashMap<>();

    @Nullable
    @Override
    public List<TreeReference> filter(DataInstance sourceInstance, TreeReference nodeSet, XPathExpression predicate, List<TreeReference> children, EvaluationContext evaluationContext) {
        String key = getKey(nodeSet, predicate);
        return cachedEvaluations.getOrDefault(key, null);
    }

    @Override
    public void cache(TreeReference nodeSet, XPathExpression predicate, List<TreeReference> treeReferences) {
        String key = getKey(nodeSet, predicate);
        if (isCacheable(predicate)) {
            cachedEvaluations.put(key, treeReferences);
        }
    }

    private String getKey(TreeReference nodeSet, XPathExpression predicate) {
        return nodeSet.toString() + predicate.toString();
    }

    private boolean isCacheable(XPathExpression predicate) {
        return predicate.isIdempotent();
    }
}
