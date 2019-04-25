package org.javarosa.core.model.instance;

import static org.javarosa.xpath.expr.XPathPathExpr.INIT_CONTEXT_RELATIVE;

import org.javarosa.model.xform.XPathReference;
import org.javarosa.xpath.expr.XPathPathExpr;
import org.javarosa.xpath.expr.XPathStep;

public class TestHelpers {
    static TreeReference buildRef(String xpath) {
        return xpath.isEmpty()
            // Support for an empty xpath, mapping it to a relative TreeReference with no steps in it.
            ? new XPathPathExpr(INIT_CONTEXT_RELATIVE, new XPathStep[0]).getReference()
            : (TreeReference) new XPathReference(xpath).getReference();
    }
}
