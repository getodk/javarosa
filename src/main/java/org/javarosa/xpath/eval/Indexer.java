package org.javarosa.xpath.eval;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xpath.expr.XPathPathExpr;

import java.util.List;

public interface Indexer {

    void prepIndex();

    void addToIndex(TreeReference currentTreeReference, TreeElement currentTreeElement);

    List<TreeReference> resolveFromIndex(TreeReference treeReference);

    IAnswerData getRawValueFromIndex(TreeReference treeReference);

    boolean belong(TreeReference currentTreeReference);

    XPathPathExpr getExpression();

    void finalizeIndex();

    void deleteIndex();

    IndexerType getIndexerType();

    PredicateStep[] getPredicateSteps();

}
