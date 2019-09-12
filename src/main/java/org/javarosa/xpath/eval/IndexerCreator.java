package org.javarosa.xpath.eval;

import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xpath.expr.XPathPathExpr;

public interface IndexerCreator {

    Indexer getIndexer(IndexerType indexType, XPathPathExpr xPathPathExpr, TreeReference expressionRef, TreeReference resultRef);

    Indexer getIndexer(IndexerType indexType, XPathPathExpr xPathPathExpr, TreeReference expressionRef, TreeReference resultRef, PredicateStep[] predicateSteps);

}
