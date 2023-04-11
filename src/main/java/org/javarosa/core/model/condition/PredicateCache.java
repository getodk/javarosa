package org.javarosa.core.model.condition;

import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xpath.expr.XPathExpression;

import java.util.List;

public interface PredicateCache {

    void cache(TreeReference nodeSet, XPathExpression predicate, List<TreeReference> treeReferences);
}
