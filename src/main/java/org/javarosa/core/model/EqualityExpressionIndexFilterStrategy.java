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

        CompareChildToAbsoluteExpression candidate = CompareChildToAbsoluteExpression.parse(predicate);
        if (candidate != null) {
            XPathEqExpr original = (XPathEqExpr) candidate.getOriginal();
            if (original.isEqual()) {
                String section = nodeSet + candidate.getRelativeSide().toString();
                if (!index.contains(section)) {
                    buildIndex(sourceInstance, candidate, children, evaluationContext, section);
                }

                Object absoluteValue = candidate.evalAbsolute(sourceInstance, evaluationContext);
                return index.lookup(section, absoluteValue.toString());
            } else {
                return next.get();
            }
        } else {
            return next.get();
        }
    }

    private void buildIndex(DataInstance sourceInstance, CompareChildToAbsoluteExpression predicate, List<TreeReference> children, EvaluationContext evaluationContext, String section) {
        for (int i = 0; i < children.size(); i++) {
            TreeReference child = children.get(i);

            Measure.log("IndexEvaluation");
            String relativeValue = predicate.evalRelative(sourceInstance, evaluationContext, child, i).toString();
            index.add(section, relativeValue, child);
        }
    }

    private static class InMemTreeReferenceIndex {

        private final Map<String, Map<String, List<TreeReference>>> map = new HashMap<>();

        public boolean contains(String section) {
            return map.containsKey(section);
        }

        public void add(String section, String key, TreeReference reference) {
            if (!map.containsKey(section)) {
                map.put(section, new HashMap<>());
            }

            Map<String, List<TreeReference>> sectionMap = map.get(section);
            if (!sectionMap.containsKey(key)) {
                sectionMap.put(key, new ArrayList<>());
            }

            sectionMap.get(key).add(reference);
        }

        public List<TreeReference> lookup(String section, String key) {
            if (map.containsKey(section) && map.get(section).containsKey(key)) {
                return map.get(section).get(key);
            } else {
                return emptyList();
            }
        }
    }
}
