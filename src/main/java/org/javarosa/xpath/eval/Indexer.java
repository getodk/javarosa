package org.javarosa.xpath.eval;

import org.javarosa.core.model.condition.IConditionExpr;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xml.TreeElementParser;
import org.javarosa.xpath.XPathConditional;
import org.javarosa.xpath.expr.XPathEqExpr;
import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.expr.XPathPathExpr;
import org.javarosa.xpath.expr.XPathQName;
import org.javarosa.xpath.expr.XPathStep;
import org.javarosa.xpath.expr.XPathStringLiteral;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @johnthebeloved
 *
 * Represents an indexed xpath expression which could be used to make expression evaluation faster
 * since the pre-evaluated expressions are stored in this index
 *
 * Used for pre-evaluating and indexing the pre-evaluated expression patterns,
 * so that results of expression evaluation during initialization and loading and filling of form can be
 * fetched from this index instead of
 *
 * Currently works for
 * ItemsetBinding.nodeset(nodeset attribute of <strong>itemsets</strong>) and
 * Recalculate(calculate attribute of <strong>bind</strong>)
 *
 */
public class Indexer {

    public TreeReference expressionRef; //The genericised expression to be indexed - used as the key
    public String expressionString;
    public TreeReference resultRef;  //The genericised pattern of the result to be cached
    public String resultString;
    public PredicateStep[] predicateSteps; //The predicates applied to the expression
    public IndexerType indexerType; // Used to determine how expression would be indexed
    public Map<TreeReference, List<TreeReference>> nodesetExprDict; // Map  used if result is a list of collated nodeset refs
    public Map<TreeReference, IAnswerData> rawValueExprDict; // Used if indexed refs are single Answers

   //Used to keep keys/values before values/keys are reached
    private Map<TreeReference, TreeReference> tempKeyKepper = new HashMap();
    private Map<TreeReference, IAnswerData> tempValueKepper = new HashMap();

    public Indexer(IndexerType indexType, TreeReference expressionRef, TreeReference resultRef) {
        this(indexType, expressionRef, resultRef, null);
    }

    public Indexer(IndexerType indexType, TreeReference expressionRef, TreeReference resultRef, PredicateStep[] predicateSteps) {
        this.expressionRef = expressionRef.removePredicates().genericize();
        this.expressionString = expressionRef.toString();
        this.resultRef = resultRef.removePredicates().genericize();
        this.resultString = resultRef.toString();
        this.indexerType = indexType;
        this.predicateSteps = predicateSteps == null ? new PredicateStep[0] : predicateSteps;
        nodesetExprDict = new HashMap<>();
        rawValueExprDict = new HashMap<>();
    }

    public static Indexer getIndexer(XPathPathExpr xPathPathExpr) {
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
            return new Indexer(IndexerType.GENERIC_PATH, keyValueRef,keyValueRef);
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
                    return new Indexer(IndexerType.LAST_EQUAL_PREDICATE_PATH, keyRef, valueRef,
                        Arrays.asList(new PredicateStep(lastStepIndex, predicateXpression)).toArray(new PredicateStep[0]));
                }
            }
        } else if (singleMiddlePredicate) {
            int predicateStepIndex = -1;
            for(int i = 0; i <  xPathPathExpr.steps.length; i++){
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
                return new Indexer(IndexerType.SINGLE_MID_EQUAL_PREDICATE_PATH, keyRef, valueRef,
                    Arrays.asList(new PredicateStep(predicateStepIndex, predicateXpression)).toArray(new PredicateStep[1]));
            }
        }

        return null;

    }



    public void addToIndex(TreeReference currentTreeReference, TreeElement currentTreeElement) {
        if (indexerType == IndexerType.GENERIC_PATH) {
            if (nodesetExprDict.get(expressionRef) == null) {
                nodesetExprDict.put(expressionRef, new ArrayList<>());
            }
            List<TreeReference> matches = nodesetExprDict.get(expressionRef);
            //TODO: equate with resultRef here instead of removing last, but this is correct since it's last - see trimToLevel
            matches.add(currentTreeReference);
        } else if (indexerType == IndexerType.LAST_EQUAL_PREDICATE_PATH) {
            if (currentTreeReference.genericize().removePredicates().equals(expressionRef)) {
                //TODO: .genericise also clones
                TreeReference currentReferenceClone = currentTreeReference.clone();
                TreeReference expressionRefIndex = withPredicates(currentReferenceClone, currentTreeElement.getValue().getDisplayText());

                TreeReference valueRef = currentTreeReference.getParentRef();
                if (nodesetExprDict.get(expressionRefIndex) == null) {
                    nodesetExprDict.put(expressionRefIndex, new ArrayList<>());
                }
                List<TreeReference> matches = nodesetExprDict.get(expressionRefIndex);
                //TODO: equate with resultRef here instead of removing last, but this is correct since it's last - see trimToLevel
                matches.add(valueRef);
            }


        } else if (indexerType == IndexerType.SINGLE_MID_EQUAL_PREDICATE_PATH) {
            if (currentTreeReference.genericize().removePredicates().equals(expressionRef)) {
                TreeReference currentReferenceClone = currentTreeReference.clone();
                TreeReference indexKey = withPredicates(currentReferenceClone, currentTreeElement.getValue() != null ? currentTreeElement.getValue().getDisplayText() : null);
                IAnswerData valueRef = tempValueKepper.get(currentTreeReference.getParentRef());
                boolean valueRefFound = valueRef != null;
                if (valueRefFound) {
                    if (nodesetExprDict.get(indexKey) == null) {
                        nodesetExprDict.put(indexKey, new ArrayList<>());
                    }
                    rawValueExprDict.put(indexKey, valueRef);
                } else {
                    //Put the common parent as the key
                    tempKeyKepper.put(currentTreeReference.getParentRef(), indexKey);
                }
            } else if(currentTreeReference.genericize().removePredicates().equals(resultRef)){
                TreeReference keyRef = tempKeyKepper.get(currentTreeReference.getParentRef());
                boolean keyRefFound = keyRef != null && keyRef.genericize().removePredicates().equals(expressionRef);
                if (keyRefFound ) {
                    rawValueExprDict.put(keyRef, currentTreeElement.getValue());
                }else{
                    tempValueKepper.put(currentTreeReference.getParentRef(), currentTreeElement.getValue());
                }

            }
        }
    }

    public List<TreeReference> getFromIndex(TreeReference treeReference) {
        return nodesetExprDict.get(treeReference);
    }

    public IAnswerData getRawValueFromIndex(TreeReference treeReference) {
        return rawValueExprDict.get(treeReference);
    }

    public boolean belong(TreeReference currentTreeReference) {
        String instanceName = currentTreeReference.getInstanceName();
        if(instanceName != null && !instanceName.equals(expressionRef.getInstanceName())){
            return  false;
        }
        String treeRefString = currentTreeReference.toString(false);
        if (indexerType.equals(IndexerType.GENERIC_PATH) ||
            indexerType.equals(IndexerType
                .LAST_EQUAL_PREDICATE_PATH)
        ) {
            return treeRefString.equals(expressionString) ||
                treeRefString.equals(resultString);
        }else if (indexerType.equals(IndexerType.SINGLE_MID_EQUAL_PREDICATE_PATH)) {
            return treeRefString.equals(expressionString) ||
                treeRefString.equals(resultString);
        }
        return false;
    }

    TreeReference withPredicates(TreeReference refToIndex, String value) {
        if(value == null){ return null; }
        if (indexerType == IndexerType.GENERIC_PATH) {
            return expressionRef;
        } else if (indexerType == IndexerType.LAST_EQUAL_PREDICATE_PATH) {

            PredicateStep predicateStep = predicateSteps[0];
            TreeReference genericizedRefToIndex = refToIndex.genericize();
            String refLastLevel = ((XPathPathExpr) ((XPathEqExpr) predicateStep.predicate).a).getReference().getNameLast();

            if (expressionRef.equals(genericizedRefToIndex)) {
                XPathStep[] xPathSteps = new XPathStep[]{new XPathStep(XPathStep.AXIS_CHILD, new XPathQName(refLastLevel))};
                XPathPathExpr a = new XPathPathExpr(XPathPathExpr.INIT_CONTEXT_RELATIVE, xPathSteps);
                XPathStringLiteral b = new XPathStringLiteral(value);
                XPathEqExpr xPathEqExpr = new XPathEqExpr(true, a, b);
                genericizedRefToIndex.addPredicate(predicateStep.stepIndex, Arrays.asList(xPathEqExpr));
                genericizedRefToIndex.removeLastLevel();
                return genericizedRefToIndex;
            }

        } else if (indexerType == IndexerType.SINGLE_MID_EQUAL_PREDICATE_PATH) {

            PredicateStep predicateStep = predicateSteps[0];
            TreeReference genericizedRefToIndex = refToIndex.genericize();
            String refLastLevel = ((XPathPathExpr) ((XPathEqExpr) predicateStep.predicate).a).getReference().getNameLast();

            if (expressionRef.equals(genericizedRefToIndex)) {
                XPathStep[] xPathSteps = new XPathStep[]{new XPathStep(XPathStep.AXIS_CHILD, new XPathQName(refLastLevel))};
                XPathPathExpr a = new XPathPathExpr(XPathPathExpr.INIT_CONTEXT_RELATIVE, xPathSteps);
                XPathStringLiteral b = new XPathStringLiteral(value);
                XPathEqExpr xPathEqExpr = new XPathEqExpr(true, a, b);
                genericizedRefToIndex.addPredicate(predicateStep.stepIndex, Arrays.asList(xPathEqExpr));
                genericizedRefToIndex.removeLastLevel();
                genericizedRefToIndex.add("label", -1);
                return genericizedRefToIndex;
            }
        }
        return null;
    }

    public static XPathPathExpr getXPathPathExpr(IConditionExpr iConditionExpr){
        if(iConditionExpr instanceof XPathConditional){
            XPathConditional xPathConditional =  (XPathConditional) iConditionExpr;
            if(xPathConditional.getExpr() instanceof XPathPathExpr){
                return (XPathPathExpr) xPathConditional.getExpr();
            }
        }
        return null;
    }

    public void clearCaches(){
        tempValueKepper.clear();
        tempKeyKepper.clear();
    }

    public static void keepIndex(IConditionExpr xPathExpression){
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

}
