package org.javarosa.core.model;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.PredicateFilter;
import org.javarosa.core.model.instance.DataInstance;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.measure.Measure;
import org.javarosa.xpath.expr.XPathEqExpr;
import org.javarosa.xpath.expr.XPathExpression;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * Uses a (lazily constructed) index to evaluate a predicate for supported expressions - currently just
 * {@link XPathEqExpr} where one side is relative to the instance child being filtered. Evaluations are fetched in
 * O(1) time with O(n) expression evaluations only being required the first time a relative side is evaluated.
 */
public class IndexPredicateFilter implements PredicateFilter {

    private final TreeReferenceIndex index;

    public IndexPredicateFilter(TreeReferenceIndex treeReferenceIndex) {
        this.index = treeReferenceIndex;
    }

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
                String section = sourceInstance.getInstanceId() + nodeSet + candidate.getRelativeSide().toString();
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

    private static boolean isNested(TreeReference nodeSet) {
        for (int i = 1; i < nodeSet.size(); i++) {
            if (nodeSet.getMultiplicity(i) > -1) {
                return true;
            }
        }

        return false;
    }

    private void buildIndex(DataInstance sourceInstance, CompareChildToAbsoluteExpression predicate, List<TreeReference> children, EvaluationContext evaluationContext, String section) {
        for (int i = 0; i < children.size(); i++) {
            TreeReference child = children.get(i);

            Measure.log("IndexEvaluation");
            String relativeValue = predicate.evalRelative(sourceInstance, evaluationContext, child, i).toString();
            index.add(section, relativeValue, child);
        }
    }

    interface TreeReferenceIndex {
        boolean contains(String section);

        void add(String section, String key, TreeReference reference);

        List<TreeReference> lookup(String section, String key);
    }
}
