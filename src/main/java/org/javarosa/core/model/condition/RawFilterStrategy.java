package org.javarosa.core.model.condition;

import org.javarosa.core.model.instance.DataInstance;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.measure.Measure;
import org.javarosa.xpath.expr.XPathExpression;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

class RawFilterStrategy implements FilterStrategy {

    @NotNull
    @Override
    public List<TreeReference> filter(@NotNull DataInstance sourceInstance,
                                      @NotNull TreeReference nodeSet,
                                      @NotNull XPathExpression predicate,
                                      @NotNull List<TreeReference> children,
                                      @NotNull EvaluationContext evaluationContext,
                                      @NotNull Supplier<List<TreeReference>> nextFilter) {
        List<TreeReference> predicatePassed = new ArrayList<>(children.size());
        for (int i = 0; i < children.size(); ++i) {
            //if there are predicates then we need to see if e.nextElement meets the standard of the predicate
            TreeReference treeRef = children.get(i);

            //test the predicate on the treeElement
            EvaluationContext evalContext = evaluationContext.rescope(treeRef, i);

            Measure.log("PredicateEvaluation");
            Object o = predicate.eval(sourceInstance, evalContext);

            if (o instanceof Boolean) {
                boolean testOutcome = (Boolean) o;
                if (testOutcome) {
                    predicatePassed.add(treeRef);
                }
            }
        }

        return predicatePassed;
    }
}
