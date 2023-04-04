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

    public Map<XPathExpression, List<TreeReference>> cachedEvaluations = new HashMap<>();

    @Override
    @NotNull
    public List<TreeReference> get(XPathExpression predicate, Supplier<List<TreeReference>> onMiss) {
        if (cachedEvaluations.containsKey(predicate)) {
            return cachedEvaluations.get(predicate);
        } else {
            List<TreeReference> references = onMiss.get();
            if (isCacheable(predicate)) {
                cachedEvaluations.put(predicate, references);
            }

            return references;
        }
    }

    private boolean isCacheable(XPathExpression predicate) {
        return predicate instanceof XPathEqExpr &&
            !(((XPathEqExpr) predicate).a instanceof XPathFuncExpr) &&
            !(((XPathEqExpr) predicate).b instanceof XPathFuncExpr);
    }
}
