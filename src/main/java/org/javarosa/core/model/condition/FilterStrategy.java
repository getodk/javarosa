package org.javarosa.core.model.condition;

import org.javarosa.core.model.instance.DataInstance;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xpath.expr.XPathExpression;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public interface FilterStrategy {

    @NotNull
    List<TreeReference> filter(@NotNull DataInstance sourceInstance,
                               @NotNull TreeReference nodeSet,
                               @NotNull XPathExpression predicate,
                               @NotNull List<TreeReference> children,
                               @NotNull EvaluationContext evaluationContext,
                               @NotNull Supplier<List<TreeReference>> next);
}
