package org.javarosa.core.model.condition;

import org.javarosa.core.model.instance.DataInstance;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xpath.expr.XPathExpression;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CachingPredicateFilter implements PredicateFilter {

    private final PredicateCache cache;
    private final PredicateFilter filter;

    public CachingPredicateFilter(PredicateCache cache, PredicateFilter filter) {
        this.cache = cache;
        this.filter = filter;
    }

    @Nullable
    @Override
    public List<TreeReference> filter(DataInstance sourceInstance, TreeReference treeReference, XPathExpression predicate, List<TreeReference> children, EvaluationContext evaluationContext) {
        List<TreeReference> filtered = filter.filter(sourceInstance, treeReference, predicate, children, evaluationContext);
        cache.cache(treeReference, predicate, filtered);
        return filtered;
    }
}
