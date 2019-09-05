package org.javarosa.xpath.eval;

import org.javarosa.core.model.condition.IConditionExpr;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xform.parse.XFormParser;
import org.javarosa.xml.TreeElementParser;
import org.javarosa.xpath.XPathConditional;
import org.javarosa.xpath.expr.XPathEqExpr;
import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.expr.XPathPathExpr;
import org.javarosa.xpath.expr.XPathStep;

import java.util.Arrays;
import java.util.List;

public interface Indexer {

    static Indexer getIndexer(XPathPathExpr xPathPathExpr) {
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
            return XFormParser.indexerCreator.getIndexer(IndexerType.GENERIC_PATH, keyValueRef,keyValueRef);
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
                    return XFormParser.indexerCreator.getIndexer(IndexerType.LAST_EQUAL_PREDICATE_PATH, keyRef, valueRef,
                        Arrays.asList(new PredicateStep(lastStepIndex, predicateXpression)).toArray(new PredicateStep[0]));
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
                return XFormParser.indexerCreator.getIndexer(IndexerType.SINGLE_MID_EQUAL_PREDICATE_PATH, keyRef, valueRef,
                    Arrays.asList(new PredicateStep(predicateStepIndex, predicateXpression)).toArray(new PredicateStep[1]));
            }
        }

        return null;

    }

    static XPathPathExpr getXPathPathExpr(IConditionExpr iConditionExpr){
        if(iConditionExpr instanceof XPathConditional){
            XPathConditional xPathConditional =  (XPathConditional) iConditionExpr;
            if(xPathConditional.getExpr() instanceof XPathPathExpr){
                return (XPathPathExpr) xPathConditional.getExpr();
            }
        }
        return null;
    }

    static void keepIndex(IConditionExpr xPathExpression){
        XPathPathExpr nodesetXPathPathExpr = Indexer.getXPathPathExpr(xPathExpression);
        if(nodesetXPathPathExpr != null){
            Indexer indexer = getIndexer(nodesetXPathPathExpr);
            if(indexer != null && !TreeElementParser.indexedExpressions.contains(xPathExpression)){
                TreeElementParser
                    .indexers.add(indexer);
                TreeElementParser.indexedExpressions.add(xPathExpression);
            }
        }
    }

    void addToIndex(TreeReference currentTreeReference, TreeElement currentTreeElement);

    List<TreeReference> getFromIndex(TreeReference treeReference);

    IAnswerData getRawValueFromIndex(TreeReference treeReference);

    boolean belong(TreeReference currentTreeReference);

    void clearCaches();

    public IndexerType getIndexerType();

    public PredicateStep[] getPredicateSteps();

}
