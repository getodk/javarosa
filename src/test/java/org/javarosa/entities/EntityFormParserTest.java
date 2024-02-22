package org.javarosa.entities;

import kotlin.Pair;
import org.javarosa.core.model.FormDef;
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
    public void parseFirstDatasetToCreate_findsCreateWithTrueString() throws XFormParser.ParseException {
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

        String dataset = EntityFormParser.parseFirstDatasetToCreate(EntityFormParser.getEntityElement(formDef.getMainInstance()));
        assertThat(dataset, equalTo("people"));
    }
}