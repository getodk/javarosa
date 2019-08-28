package org.javarosa.xpath.eval;

import org.javarosa.xpath.expr.XPathExpression;

public class PredicateStep {

    public PredicateStep(int stepIndex, XPathExpression predicate){
        this.stepIndex = stepIndex;
        this.predicate = predicate;
    }

    public int stepIndex;
    public XPathExpression predicate;

}
