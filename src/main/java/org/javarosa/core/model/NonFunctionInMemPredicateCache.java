package org.javarosa.core.model;

import org.javarosa.core.model.condition.PredicateCache;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xpath.expr.XPathEqExpr;
import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 *  In memory implementation of a {@link PredicateCache}. Cannot cache predicate evaluations where either side is
 *  a function.
 */
public class NonFunctionInMemPredicateCache implements PredicateCache {

    public Map<String, List<TreeReference>> cachedEvaluations = new HashMap<>();

    @Override
    @NotNull
    public List<TreeReference> get(TreeReference predicateTarget, XPathExpression predicate, Supplier<List<TreeReference>> onMiss) {
        String key = getKey(predicateTarget, predicate);

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

    private String getKey(TreeReference reference, XPathExpression predicate) {
        return reference.toString() + predicate.toString();
    }

    private boolean isCacheable(XPathExpression predicate) {
        return predicate instanceof XPathEqExpr &&
            !(((XPathEqExpr) predicate).a instanceof XPathFuncExpr) &&
            !(((XPathEqExpr) predicate).b instanceof XPathFuncExpr);
    }
}
