package org.javarosa.core.model;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.PredicateFilter;
import org.javarosa.core.model.instance.DataInstance;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xpath.expr.XPathEqExpr;
import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.expr.XPathPathExpr;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class IndexingPredicateFilter implements PredicateFilter {

    private final Map<String, List<TreeReference>> cachedEvaluations = new HashMap<>();

    @Nullable
    @Override
    public List<TreeReference> filter(DataInstance sourceInstance, TreeReference treeReference, XPathExpression predicate, List<TreeReference> children, EvaluationContext evaluationContext, Supplier<List<TreeReference>> next) {
        String key = null;
        if (predicate instanceof XPathEqExpr &&
            ((XPathEqExpr) predicate).a instanceof XPathPathExpr &&
            ((XPathEqExpr) predicate).b instanceof XPathPathExpr &&
            ((XPathPathExpr) ((XPathEqExpr) predicate).a).init_context == XPathPathExpr.INIT_CONTEXT_RELATIVE) {
            XPathPathExpr left = (XPathPathExpr) ((XPathEqExpr) predicate).a;
            XPathPathExpr right = (XPathPathExpr) ((XPathEqExpr) predicate).b;

            Object rightValue = right.eval(sourceInstance, evaluationContext).unpack();
            key = treeReference.toString() + left.toString() + rightValue.toString();
        }

        if (key != null) {
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
