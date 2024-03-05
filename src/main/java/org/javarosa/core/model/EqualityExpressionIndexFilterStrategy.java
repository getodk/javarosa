package org.javarosa.core.model;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.FilterStrategy;
import org.javarosa.core.model.instance.DataInstance;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.measure.Measure;
import org.javarosa.xpath.expr.XPathEqExpr;
import org.javarosa.xpath.expr.XPathExpression;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.Collections.emptyList;

/**
 * Uses a (lazily constructed) index to evaluate a predicate for supported expressions - currently just
 * {@link XPathEqExpr} where one side is relative to the instance child being filtered. Evaluations are fetched in
 * O(1) time with O(n) expression evaluations only being required the first time a relative side is evaluated.
 */
public class EqualityExpressionIndexFilterStrategy implements FilterStrategy {

    private final InMemTreeReferenceIndex index = new InMemTreeReferenceIndex();

    @NotNull
    @Override
    public List<TreeReference> filter(@NotNull DataInstance sourceInstance, @NotNull TreeReference nodeSet, @NotNull XPathExpression predicate, @NotNull List<TreeReference> children, @NotNull EvaluationContext evaluationContext, @NotNull Supplier<List<TreeReference>> next) {
        if (sourceInstance.getInstanceId() == null || !(predicate instanceof XPathEqExpr)) {
            return next.get();
        }

        CompareToNodeExpression candidate = CompareToNodeExpression.parse(predicate);
        if (candidate != null) {
            XPathEqExpr original = (XPathEqExpr) candidate.getOriginal();
            if (original.isEqual()) {
                String section = nodeSet + candidate.getNodeSide().toString();
                buildIndexIfNeeded(sourceInstance, candidate, children, evaluationContext, section);

                Object absoluteValue = candidate.evalContextSide(sourceInstance, evaluationContext);

                if (absoluteValue instanceof String) {
                    return index.lookup(section, (String) absoluteValue);
                } else {
                    return next.get();
                }
            } else {
                return next.get();
            }
        } else {
            return next.get();
        }
    }

    /**
     * Synchronized to prevent two or more threads from modifying the index at once
     */
    private synchronized void buildIndexIfNeeded(DataInstance sourceInstance, CompareToNodeExpression predicate, List<TreeReference> children, EvaluationContext evaluationContext, String section) {
        if (!index.contains(section)) {
            for (int i = 0; i < children.size(); i++) {
                TreeReference child = children.get(i);

                Measure.log("IndexEvaluation");
                String relativeValue = predicate.evalNodeSide(sourceInstance, evaluationContext, child, i).toString();
                index.add(section, relativeValue, child);
            }
        }
    }

    /**
     * Non thread safe index for tree references based on nested string keys (a "section" and an "item").
     */
    private static class InMemTreeReferenceIndex {

        private final Map<String, Map<String, List<TreeReference>>> map = new HashMap<>();

        public boolean contains(String section) {
            return map.containsKey(section);
        }

        public void add(String section, String item, TreeReference reference) {
            if (!map.containsKey(section)) {
                map.put(section, new HashMap<>());
            }

            Map<String, List<TreeReference>> sectionMap = map.get(section);
            if (!sectionMap.containsKey(item)) {
                sectionMap.put(item, new ArrayList<>());
            }

            sectionMap.get(item).add(reference);
        }

        public List<TreeReference> lookup(String section, String item) {
            if (map.containsKey(section) && map.get(section).containsKey(item)) {
                return map.get(section).get(item);
            } else {
                return emptyList();
            }
        }
    }
}
