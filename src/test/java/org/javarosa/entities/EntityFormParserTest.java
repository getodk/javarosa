package org.javarosa.entities;

import kotlin.Pair;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.util.XFormsElement;
import org.javarosa.entities.internal.EntityFormParser;
import org.javarosa.xform.parse.XFormParser;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.javarosa.core.util.BindBuilderXFormsElement.bind;
import static org.javarosa.core.util.XFormsElement.body;
import static org.javarosa.core.util.XFormsElement.head;
import static org.javarosa.core.util.XFormsElement.input;
import static org.javarosa.core.util.XFormsElement.mainInstance;
import static org.javarosa.core.util.XFormsElement.model;
import static org.javarosa.core.util.XFormsElement.t;
import static org.javarosa.core.util.XFormsElement.title;

public class EntityFormParserTest {

    @Test
    public void parseAction_findsCreateWithTrueString() throws XFormParser.ParseException {
        XFormsElement form = XFormsElement.html(
            asList(
                new Pair<>("entities", "http://www.opendatakit.org/xforms/entities")
            ),
            head(
                title("Create entity form"),
                model(
                    mainInstance(
                        t("data id=\"create-entity-form\"",
                            t("name"),
                            t("meta",
                                t("entity dataset=\"people\" create=\"true\"")
                            )
                        )
                    ),
                    bind("/data/name").type("string").withAttribute("entities", "saveto", "name")
                )
            ),
            body(
                input("/data/name")
            )
        );

        XFormParser parser = new XFormParser(new InputStreamReader(new ByteArrayInputStream(form.asXml().getBytes())));
        FormDef formDef = parser.parse(null);

        EntityAction action = EntityFormParser.parseAction(EntityFormParser.getEntityElement(formDef.getMainInstance()));
        assertThat(action, equalTo(EntityAction.CREATE));
    }

    @Test
    public void parseAction_findsUpdateWithTrueString() throws XFormParser.ParseException {
        XFormsElement form = XFormsElement.html(
            asList(
                new Pair<>("entities", "http://www.opendatakit.org/xforms/entities")
            ),
            head(
                title("Create entity form"),
                model(
                    mainInstance(
                        t("data id=\"create-entity-form\"",
                            t("name"),
                            t("meta",
                                t("entity dataset=\"people\" update=\"true\"")
                            )
                        )
                    ),
                    bind("/data/name").type("string").withAttribute("entities", "saveto", "name")
                )
            ),
            body(
                input("/data/name")
            )
        );

        XFormParser parser = new XFormParser(new InputStreamReader(new ByteArrayInputStream(form.asXml().getBytes())));
        FormDef formDef = parser.parse(null);

        EntityAction dataset = EntityFormParser.parseAction(EntityFormParser.getEntityElement(formDef.getMainInstance()));
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