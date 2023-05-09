package org.javarosa.core.model;

import kotlin.Pair;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.PredicateFilter;
import org.javarosa.core.model.instance.DataInstance;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.measure.Measure;
import org.javarosa.xpath.expr.XPathEqExpr;
import org.javarosa.xpath.expr.XPathExpression;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Uses a (lazily constructed) index to evaluate a predicate for supported expressions - currently just
 * {@link XPathEqExpr} where one side is relative to the instance child being filtered. Evaluations are fetched in
 * O(1) time with O(n) expression evaluations only being required the first time a relative side is evaluated.
 */
public class IndexPredicateFilter implements PredicateFilter {

    private final Map<Pair<String, String>, Map<String, List<TreeReference>>> instanceEqIndexes = new HashMap<>();

    @Nullable
    @Override
    public List<TreeReference> filter(DataInstance sourceInstance, TreeReference nodeSet, XPathExpression predicate, List<TreeReference> children, EvaluationContext evaluationContext, Supplier<List<TreeReference>> next) {
        if (sourceInstance.getInstanceId() == null || isNested(nodeSet) || !(predicate instanceof XPathEqExpr)) {
            return next.get();
        }

        CompareChildToAbsoluteExpression candidate = CompareChildToAbsoluteExpression.parse(predicate);
        if (candidate != null) {
            XPathEqExpr original = (XPathEqExpr) candidate.getOriginal();
            if (original.isEqual()) {
                Pair<String, String> indexKey = new Pair<>(sourceInstance.getInstanceId(), nodeSet.toString() + candidate.getRelativeSide().toString());
                if (!instanceEqIndexes.containsKey(indexKey)) {
                    instanceEqIndexes.put(indexKey, new HashMap<>());
                }

                Map<String, List<TreeReference>> index = instanceEqIndexes.get(indexKey);
                if (index.isEmpty()) {
                    buildEqIndex(sourceInstance, candidate, children, evaluationContext, index);
                }

                Object absoluteValue = candidate.evalAbsolute(sourceInstance, evaluationContext);
                return index.getOrDefault(absoluteValue.toString(), new ArrayList<>());
            } else {
                return next.get();
            }
        } else {
            return next.get();
        }
    }

    private static boolean isNested(TreeReference nodeSet) {
        for (int i = 1; i < nodeSet.size(); i++) {
            if (nodeSet.getMultiplicity(i) > -1) {
                return true;
            }
        }

        return false;
    }

    private static void buildEqIndex(DataInstance sourceInstance, CompareChildToAbsoluteExpression predicate, List<TreeReference> children, EvaluationContext evaluationContext, Map<String, List<TreeReference>> eqIndex) {
        for (int i = 0; i < children.size(); i++) {
            TreeReference child = children.get(i);

            Measure.log("IndexEvaluation");
            String relativeValue = predicate.evalRelative(sourceInstance, evaluationContext, child, i).toString();

            if (!eqIndex.containsKey(predicate.getRelativeSide().toString())) {
                eqIndex.put(relativeValue, new ArrayList<>());
            }

            eqIndex.get(relativeValue).add(child);
        }
    }
}
