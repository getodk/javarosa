package org.javarosa.core.model.condition;

import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xpath.expr.XPathExpression;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

/**
 * Allows the result of predicate evaluations (references for matching nodes) to be cached. The cache doesn't know
 * anything about the values that might be referenced in a predicate (the "triggerables"), so can only be used in cases
 * where the predicates are "static".
 */
public interface PredicateCache {

    @NotNull
    List<TreeReference> get(TreeReference position, String childName, XPathExpression predicate, Supplier<List<TreeReference>> onMiss);
}
