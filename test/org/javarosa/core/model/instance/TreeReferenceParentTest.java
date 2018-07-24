package org.javarosa.core.model.instance;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.javarosa.xpath.expr.XPathPathExpr.INIT_CONTEXT_RELATIVE;
import static org.javarosa.xpath.expr.XPathPathExpr.INIT_CONTEXT_ROOT;
import static org.javarosa.xpath.expr.XPathStep.ABBR_PARENT;
import static org.javarosa.xpath.expr.XPathStep.ABBR_SELF;
import static org.javarosa.xpath.expr.XPathStep.AXIS_CHILD;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.javarosa.xpath.expr.XPathPathExpr;
import org.javarosa.xpath.expr.XPathQName;
import org.javarosa.xpath.expr.XPathStep;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class TreeReferenceParentTest {

    @Parameterized.Parameter(value = 0)
    public String testCase;

    @Parameterized.Parameter(value = 1)
    public String tr;

    @Parameterized.Parameter(value = 2)
    public String base;

    @Parameterized.Parameter(value = 3)
    public String expectedRef;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {"Parenting absolute refs doesn't change them", "/foo", "/bar", "/foo"},
            {"Parenting to an empty ref doesn't change them", "../foo", "", "../foo"},
            {"foo.parent(bar) gives bar/foo", "foo", "bar", "bar/foo"},
            {"foo.parent(../bar) gives ../bar/foo", "foo", "../bar", "../bar/foo"},
            // TODO review this. Shouldn't this resolve to "bar/foo"?
            {"../foo.parent(bar/baz) gives null", "../foo", "bar/baz", null},

        });
    }

    @Test
    public void parent_works_as_expected() {
        assertThat(
            buildRef(tr).parent(buildRef(base)),
            expectedRef == null
                ? is(nullValue(TreeReference.class))
                : is(buildRef(expectedRef))
        );
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