package org.javarosa.xpath.eval;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xpath.expr.XPathPathExpr;

import java.util.List;

/**
 * @johnthebeloved
 *
 * Represents an xpath expression indexer which could be used to make expression evaluation faster
 * the pre-evaluated expressions are stored in this index as an inmemory Map
 *
 * Used for pre-evaluating and indexing the pre-evaluated expression patterns,
 * so that results of expression evaluation during initialization and loading and filling of form can be
 * fetched from this index instead of
 *
 * Currently used for
 * ItemsetBinding#nodeset attribute of <strong>itemsets</strong>) and
 * Recalculate#calculate attribute of <strong>bind</strong>
 *
 */
public interface Indexer {

    /**
     * Just created the index, perform initializations
     */
    void prepIndex();

    /**
    *Add a tree element to this index
    *@param currentTreeReference : The generic TreeReference
    *param  currentTreeElement : The node to index (SHould be a leaf node)
    */
    void addToIndex(TreeReference currentTreeReference, TreeElement currentTreeElement);

    /**
     * @param treeReference get from the index
     */
    List<TreeReference> resolveFromIndex(TreeReference treeReference);

    IAnswerData getRawValueFromIndex(TreeReference treeReference);

    boolean belong(TreeReference currentTreeReference);

    XPathPathExpr getExpression();

    void finalizeIndex();

    void deleteIndex();

    IndexerType getIndexerType();

    PredicateStep[] getPredicateSteps();

}
