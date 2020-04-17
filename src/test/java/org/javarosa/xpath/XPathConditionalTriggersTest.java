package org.javarosa.xpath;

import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.test.Scenario;
import org.javarosa.xpath.parser.XPathSyntaxException;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

public class XPathConditionalTriggersTest {
    @Test
    public void predicateTriggers_areAddedToDag() throws XPathSyntaxException {
        XPathConditional expression = new XPathConditional("../inner[position() = ../node2]/node3");
        TreeReference context = Scenario.getRef("/data/outer[7]/node1");

        TreeReference predicateTrigger = Scenario.getRef("/data/outer[7]/node2");

        assertThat(expression.getTriggers(context), hasItem(predicateTrigger));
    }
}
