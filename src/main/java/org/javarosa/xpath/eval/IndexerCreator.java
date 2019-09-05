package org.javarosa.xpath.eval;

import org.javarosa.core.model.instance.TreeReference;

public interface IndexerCreator {

    Indexer getIndexer(IndexerType indexType, TreeReference expressionRef, TreeReference resultRef);

    Indexer getIndexer(IndexerType indexType, TreeReference expressionRef, TreeReference resultRef, PredicateStep[] predicateSteps);
}
