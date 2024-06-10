package org.javarosa.entities;

import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.entities.internal.EntityFormParser;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class EntityFormParserTest {

    @Test
    public void parseAction_findsCreateWithTrueString() {
        TreeElement entityElement = new TreeElement("entity");
        entityElement.setAttribute(null, "create", "true");

        EntityAction action = EntityFormParser.parseAction(entityElement);
        assertThat(action, equalTo(EntityAction.CREATE));
    }

    @Test
    public void parseAction_findsUpdateWithTrueString() {
        TreeElement entityElement = new TreeElement("entity");
        entityElement.setAttribute(null, "update", "true");

        EntityAction dataset = EntityFormParser.parseAction(entityElement);
        assertThat(dataset, equalTo(EntityAction.UPDATE));
    }

    @Test
    public void parseLabel_whenLabelIsAnInt_convertsToString() {
        TreeElement labelElement = new TreeElement("label");
        labelElement.setAnswer(new IntegerData(0));
        TreeElement entityElement = new TreeElement("entity");
        entityElement.addChild(labelElement);

        String label = EntityFormParser.parseLabel(entityElement);
        assertThat(label, equalTo("0"));
    }
}