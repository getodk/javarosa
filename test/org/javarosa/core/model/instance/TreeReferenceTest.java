package org.javarosa.core.model.instance;

import static org.javarosa.xpath.expr.XPathPathExpr.INIT_CONTEXT_RELATIVE;
import static org.javarosa.xpath.expr.XPathPathExpr.INIT_CONTEXT_ROOT;
import static org.javarosa.xpath.expr.XPathStep.ABBR_PARENT;
import static org.javarosa.xpath.expr.XPathStep.ABBR_SELF;
import static org.javarosa.xpath.expr.XPathStep.AXIS_CHILD;
import static org.junit.Assert.assertThat;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hamcrest.Matchers;
import org.javarosa.xpath.XPathException;
import org.javarosa.xpath.expr.XPathPathExpr;
import org.javarosa.xpath.expr.XPathQName;
import org.javarosa.xpath.expr.XPathStep;
import org.junit.Test;

public class TreeReferenceTest {

    @Test(expected = XPathException.class)
    public void anchoring_to_a_relative_ref_throws() {
        TreeReference tr = buildRef("some/relative/path");
        TreeReference trBase = buildRef("some/other/relative/path");
        tr.anchor(trBase);
    }

    @Test
    public void anchoring_an_absolute_ref_has_no_effect_on_it() {
        TreeReference tr = buildRef("/some/absolute/ref");
        TreeReference trBase = buildRef("/some/other/absolute/ref");
        assertThat(tr.anchor(trBase), Matchers.is(tr));
    }

    @Test(expected = XPathException.class)
    public void anchoring_to_a_too_shallow_base_throws() {
        TreeReference tr = buildRef("../../../bar");
        TreeReference trBase = buildRef("/foo/bar");
        tr.anchor(trBase);
    }

    private TreeReference buildRef(String xpath) {
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