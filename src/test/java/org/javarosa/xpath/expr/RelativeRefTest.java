package org.javarosa.xpath.expr;

import org.javarosa.xpath.XPathParseTool;
import org.javarosa.xpath.parser.XPathSyntaxException;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class RelativeRefTest {
    @Test
    public void predicateInRelativeRef_isAppliedToCorrectLevel() throws XPathSyntaxException {
        XPathExpression predicate = XPathParseTool.parseXPath("position() = ../count");
        XPathPathExpr parentRefWithPredicate = (XPathPathExpr) XPathParseTool.parseXPath("../repeat[position() = ../count]/choice");
        assertThat(parentRefWithPredicate.getReference().getPredicate(0).get(0), is(predicate));

        XPathPathExpr grandParentRefWithPredicate = (XPathPathExpr) XPathParseTool.parseXPath("../../repeat[position() = ../count]/choice");
        assertThat(grandParentRefWithPredicate.getReference().getPredicate(0).get(0), is(predicate));
    }
}
