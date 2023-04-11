package org.javarosa.core.model.condition;

import org.javarosa.core.model.instance.DataInstance;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xpath.expr.XPathExpression;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public interface PredicateFilter {

    @Nullable
    List<TreeReference> filter(DataInstance sourceInstance,
                               TreeReference nodeSet,
                               XPathExpression predicate,
                               List<TreeReference> children,
                               EvaluationContext evaluationContext,
                               Supplier<List<TreeReference>> next);
}
