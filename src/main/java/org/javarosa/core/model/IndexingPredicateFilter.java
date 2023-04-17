package org.javarosa.core.model;

import kotlin.Pair;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.PredicateFilter;
import org.javarosa.core.model.instance.DataInstance;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.measure.Measure;
import org.javarosa.xpath.expr.XPathEqExpr;
import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.expr.XPathPathExpr;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class IndexingPredicateFilter implements PredicateFilter {

    private final Map<Pair<String, String>, Map<String, List<TreeReference>>> instanceEqIndexes = new HashMap<>();

    @Nullable
    @Override
    public List<TreeReference> filter(DataInstance sourceInstance, TreeReference nodeSet, XPathExpression predicate, List<TreeReference> children, EvaluationContext evaluationContext, Supplier<List<TreeReference>> next) {
        if (predicate instanceof XPathEqExpr &&
            ((XPathEqExpr) predicate).a instanceof XPathPathExpr &&
            ((XPathEqExpr) predicate).b instanceof XPathPathExpr &&
            ((XPathPathExpr) ((XPathEqExpr) predicate).a).init_context == XPathPathExpr.INIT_CONTEXT_RELATIVE &&
            ((XPathPathExpr) ((XPathEqExpr) predicate).b).init_context == XPathPathExpr.INIT_CONTEXT_ROOT) {
            XPathPathExpr left = (XPathPathExpr) ((XPathEqExpr) predicate).a;
            Pair<String, String> indexKey = new Pair<>(sourceInstance.getInstanceId(), left.toString());
            if (!instanceEqIndexes.containsKey(indexKey)) {
                instanceEqIndexes.put(indexKey, new HashMap<>());
            }

            Map<String, List<TreeReference>> index = instanceEqIndexes.get(indexKey);
            if (index.isEmpty()) {
                buildEqIndex(sourceInstance, (XPathEqExpr) predicate, children, evaluationContext, index);
            }

            XPathPathExpr right = (XPathPathExpr) ((XPathEqExpr) predicate).b;
            String rightValue = (String) right.eval(sourceInstance, evaluationContext).unpack();
            return index.getOrDefault(rightValue, new ArrayList<>());
        } else {
            return next.get();
        }
    }

    private static void buildEqIndex(DataInstance sourceInstance, XPathEqExpr predicate, List<TreeReference> children, EvaluationContext evaluationContext, Map<String, List<TreeReference>> eqIndex) {
        for (int i = 0; i < children.size(); i++) {
            TreeReference child = children.get(i);
            XPathPathExpr left = (XPathPathExpr) predicate.a;

            EvaluationContext evalContext = evaluationContext.rescope(child, i);

            Measure.log("IndexEvaluation");
            String leftVal = (String) left.eval(sourceInstance, evalContext).unpack();

            if (!eqIndex.containsKey(left.toString())) {
                eqIndex.put(leftVal, new ArrayList<>());
            }

            eqIndex.get(leftVal).add(child);
        }
    }
}
