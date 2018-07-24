package org.javarosa.core.model.instance;

import static org.javarosa.xpath.expr.XPathPathExpr.INIT_CONTEXT_RELATIVE;
import static org.javarosa.xpath.expr.XPathPathExpr.INIT_CONTEXT_ROOT;
import static org.javarosa.xpath.expr.XPathStep.ABBR_PARENT;
import static org.javarosa.xpath.expr.XPathStep.ABBR_SELF;
import static org.javarosa.xpath.expr.XPathStep.AXIS_CHILD;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.javarosa.xpath.expr.XPathPathExpr;
import org.javarosa.xpath.expr.XPathQName;
import org.javarosa.xpath.expr.XPathStep;

public class TestHelpers {
    static TreeReference buildRef(String xpath) {
        String[] parts = xpath.split("/");
        XPathStep[] steps = Stream.of(parts)
            .map(part -> {
                switch (part) {
                    case "..":
                        return ABBR_PARENT();
                    case "":
                        return ABBR_SELF();
                    default:
                        return new XPathStep(AXIS_CHILD, new XPathQName(null, part));
                }
            })
            .collect(Collectors.toList())
            .toArray(new XPathStep[parts.length]);

        XPathPathExpr xpathExpression = new XPathPathExpr(
            xpath.startsWith("/") ? INIT_CONTEXT_ROOT : INIT_CONTEXT_RELATIVE,
            steps
        );
        return xpathExpression.getReference();
    }
}
