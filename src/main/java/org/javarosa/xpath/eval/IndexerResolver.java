package org.javarosa.xpath.eval;

import org.javarosa.core.model.condition.IConditionExpr;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xpath.XPathConditional;
import org.javarosa.xpath.expr.XPathEqExpr;
import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.expr.XPathPathExpr;
import org.javarosa.xpath.expr.XPathStep;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IndexerResolver {

    private IndexerCreator indexerCreator;
    // Added for indexing pre-evaluated expression in the form
    private List<Indexer> indexers;
    private List<IConditionExpr> indexedExpressions;

    public IndexerResolver(IndexerCreator indexerCreator){
        indexers = new ArrayList<>();
        indexedExpressions = new ArrayList<>();
        this.indexerCreator = indexerCreator;
    }

    public IndexerResolver(){
        this(new IndexerCreator() {
            @Override
            public Indexer getIndexer(IndexerType indexType, XPathPathExpr xPathPathExpr, TreeReference expressionRef, TreeReference resultRef) {
                return new MemoryIndexerImpl(indexType, xPathPathExpr, expressionRef, null);
            }

            @Override
            public Indexer getIndexer(IndexerType indexType, XPathPathExpr xPathPathExpr, TreeReference expressionRef, TreeReference resultRef, PredicateStep[] predicateSteps) {
                return new MemoryIndexerImpl(indexType, xPathPathExpr, expressionRef, resultRef, predicateSteps);
            }
        });
    }

    public List<Indexer> getIndexers(){
        return  indexers;
    }

    public List<TreeReference> getNodeset(TreeReference treeReference){
        for (Indexer indexer : indexers) {
            if(indexer.belong(treeReference)){
                List<TreeReference> nodesetReferences = indexer.resolveFromIndex(treeReference);
                if (nodesetReferences != null) {
                    return nodesetReferences;
                }
            }
        }
        return null;
    }

    public IAnswerData getRVFromIndex(TreeReference treeReference){

        for (Indexer indexer : indexers) {
            if(indexer.belong(treeReference)) {
                IAnswerData rawValue = indexer.getRawValueFromIndex(treeReference);
                if (rawValue != null) {
                    return rawValue;
                }
            }
        }
        return null;
    }

    public void indexThisExpression(IConditionExpr xPathExpression){
        XPathPathExpr nodesetXPathPathExpr = getXPathPathExpr(xPathExpression);
        if(nodesetXPathPathExpr != null){
            Indexer indexer = getIndexer(nodesetXPathPathExpr);
            if(indexer != null && !indexedExpressions.contains(xPathExpression)){
                indexers.add(indexer);
                indexedExpressions.add(xPathExpression);
            }
        }
    }

    public XPathPathExpr getXPathPathExpr(IConditionExpr iConditionExpr){
        if(iConditionExpr instanceof XPathConditional){
            XPathConditional xPathConditional =  (XPathConditional) iConditionExpr;
            if(xPathConditional.getExpr() instanceof XPathPathExpr){
                return (XPathPathExpr) xPathConditional.getExpr();
            }
        }
        return null;
    }

    public Indexer getIndexer(XPathPathExpr xPathPathExpr) {

        //Check if it's a genericized ref
        boolean isGenericPath = true;
        boolean onlyLastPredicate = false;
        boolean singleMiddlePredicate = false;
        int noOfSteps = xPathPathExpr.steps.length;
        for (int i = 0; i < noOfSteps; i++) {
            XPathStep xPathStep = xPathPathExpr.steps[i];
            if(xPathStep.axis == XPathStep.AXIS_CHILD){
                //Check if step has predicate
                if(xPathStep.predicates.length > 0){
                    //Having a predicate makes isGenericPath false
                    isGenericPath = false;
                    int lastIndex = noOfSteps - 1;
                    //Only last index has predicate
                    if (onlyLastPredicate == false && i == lastIndex) {
                        onlyLastPredicate = true;
                    } else if (singleMiddlePredicate == false && i < lastIndex){
                        singleMiddlePredicate = true;
                    } else if (singleMiddlePredicate == true && i < lastIndex){
                        singleMiddlePredicate = false;
                    } else {
                        onlyLastPredicate = false;
                    }
                }
            }
        }

        if(isGenericPath) {
            TreeReference keyValueRef = xPathPathExpr.getReference();
            Indexer indexer = indexerCreator.getIndexer(IndexerType.GENERIC_PATH, xPathPathExpr, keyValueRef,keyValueRef);
            if(indexer != null){
                indexer.prepIndex();
                return indexer;
            }
        } else if(onlyLastPredicate) {
            int lastStepIndex = noOfSteps - 1;
            XPathStep lastXPathStep = xPathPathExpr.steps[lastStepIndex];
            //We are currently handling only one predicate
            if(lastXPathStep.predicates.length == 1){
                XPathExpression predicateXpression = lastXPathStep.predicates[0];
                //We are currently handling only XPathEqExpr
                if(predicateXpression instanceof XPathEqExpr){
                    //We want to get the absolute path
                    TreeReference keyRef = ((XPathPathExpr)((XPathEqExpr) predicateXpression).a).getReference().contextualize(xPathPathExpr.getReference());
                    TreeReference valueRef = xPathPathExpr.getReference().removePredicates().genericize();
                    Indexer indexer = indexerCreator.getIndexer(IndexerType.LAST_EQUAL_PREDICATE_PATH, xPathPathExpr, keyRef, valueRef,
                        Arrays.asList(new PredicateStep(lastStepIndex, predicateXpression)).toArray(new PredicateStep[0]));
                    if(indexer != null){
                        indexer.prepIndex();
                        return indexer;
                    }
                }
            }
        } else if (singleMiddlePredicate) {
            int predicateStepIndex = -1;
            for (int i = 0; i <  xPathPathExpr.steps.length; i++) {
                XPathStep xPathStep = xPathPathExpr.steps[i];
                if(xPathStep.predicates.length > 0){
                    predicateStepIndex = i;
                    break;
                }
            }

            XPathStep midPredicateStep = xPathPathExpr.steps[predicateStepIndex];
            XPathExpression predicateXpression = midPredicateStep.predicates[0];
            //We are currently handling only XPathEqExpr
            if(predicateXpression instanceof XPathEqExpr){
                //We want to get the absolute path
                TreeReference keyRef = ((XPathPathExpr)((XPathEqExpr) predicateXpression).a).getReference().contextualize(xPathPathExpr.getReference().getSubReference(predicateStepIndex));
                TreeReference valueRef = xPathPathExpr.getReference().removePredicates();
                Indexer indexer = indexerCreator.getIndexer(IndexerType.SINGLE_MID_EQUAL_PREDICATE_PATH, xPathPathExpr, keyRef, valueRef,
                    Arrays.asList(new PredicateStep(predicateStepIndex, predicateXpression)).toArray(new PredicateStep[1]));
                if(indexer != null){
                    indexer.prepIndex();
                    return indexer;
                }
            }
        }
        return null;

    }

    //TODO:This may not be entirely correct
    public boolean refIsIndexed(TreeReference treeReference){
        for(Indexer indexer : indexers ){
            if(indexer.belong(treeReference)){
                PredicateStep[] predicateSteps = indexer.getPredicateSteps();
                if(predicateSteps.length > 0 &&
                    treeReference.getPredicate(predicateSteps[0].stepIndex) != null)
                    return true;
            }
        }
        return false;
    }

    public boolean exprIsIndexed(XPathPathExpr xPathPathExpr){
        for(Indexer indexer : indexers ){
            if(indexer.getExpression().equals(xPathPathExpr)){
                return true;
            }
        }
        return false;
    }

}
