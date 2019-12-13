package org.javarosa.core.model.instance;

import static org.javarosa.xpath.expr.XPathPathExpr.INIT_CONTEXT_RELATIVE;

import java.util.List;
import org.javarosa.model.xform.XPathReference;
import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.expr.XPathNumericLiteral;
import org.javarosa.xpath.expr.XPathPathExpr;
import org.javarosa.xpath.expr.XPathStep;

public class TestHelpers {
    public static TreeReference buildRef(String xpath) {
        // Support for an empty xpath, mapping it to a relative TreeReference with no steps in it.
        if (xpath.isEmpty())
            return new XPathPathExpr(INIT_CONTEXT_RELATIVE, new XPathStep[0]).getReference();

        TreeReference ref = (TreeReference) new XPathReference(xpath).getReference();
        // Set correct multiplicities when there's a numeric predicate pointing to an item inside an itemset as in /foo/bar[3]
        for (int i = 0; i < ref.size(); i++) {
            List<XPathExpression> predicates = ref.getPredicate(i);
            if (predicates != null && predicates.size() == 1 && predicates.get(0) instanceof XPathNumericLiteral)
                ref.setMultiplicity(i, ((Double) ((XPathNumericLiteral) predicates.get(0)).d).intValue());
        }

        return ref;
    }
}
