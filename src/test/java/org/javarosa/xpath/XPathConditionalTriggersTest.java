package org.javarosa.xpath;

import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.test.Scenario;
import org.javarosa.xpath.parser.XPathSyntaxException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;

public class XPathConditionalTriggersTest {
    @Test
    public void getTriggers_onExpressionWithRelativePathInPredicate_returnsPredicateTriggers() throws XPathSyntaxException {
        XPathConditional expression = new XPathConditional("../inner[position() = ../node2]/node3");
        TreeReference context = Scenario.getRef("/data/outer[7]/node1");

        TreeReference predicateTrigger = Scenario.getRef("/data/outer[7]/node2");

        assertThat(expression.getTriggers(context), hasItem(predicateTrigger));
    }

    @Test
    public void getTriggers_onExpressionWithComplexRelativePathInPredicate_returnsPredicateTriggers() throws XPathSyntaxException {
        XPathConditional expression = new XPathConditional("../inner[position() = anode and /data/foo = x/y/z]/anothernode");
        TreeReference context = Scenario.getRef("/data/outer[7]/bar/baz");

        assertThat(expression.getTriggers(context), hasItems(Scenario.getRef("/data/outer[7]/bar/inner/anode"),
            Scenario.getRef("/data/foo"), Scenario.getRef("/data/outer[7]/bar/inner/x/y/z")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getTriggers_onExpressionWithRelativePredicateAndNoContext_throwsError() throws XPathSyntaxException {
        XPathConditional expression = new XPathConditional("../inner[position() = ../node2]/node3");

        expression.getTriggers(null);
    }

    @Test
    public void getTriggers_onExpressionWithRelativePredicateWithCurrent_returnsTriggersContextualizedWithOriginalContext() throws XPathSyntaxException {
        XPathConditional expression = new XPathConditional("instance('dataset')/root/item[value > current()/../../node1]/name");
        TreeReference context = Scenario.getRef("/data/outer[7]/inner[3]/node2");

        TreeReference predicateTrigger = Scenario.getRef("/data/outer[7]/node1");

        assertThat(expression.getTriggers(context), hasItem(predicateTrigger));
    }
}
