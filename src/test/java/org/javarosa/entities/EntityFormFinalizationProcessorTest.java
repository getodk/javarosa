package org.javarosa.entities;

import kotlin.Pair;
import org.javarosa.core.util.XFormsElement;
import org.javarosa.entities.internal.Entities;
import org.javarosa.entities.internal.EntityFormExtra;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.model.xform.XPathReference;
import org.javarosa.xform.parse.XFormParser;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.List;

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

public class EntityFormFinalizationProcessorTest {

    @Test
    public void whenSaveToAnswerIsNull_entityPropertyIsEmptyString() throws XFormParser.ParseException {
        XFormsElement xForm =  XFormsElement.html(
            asList(
                new Pair<>("entities", "http://www.opendatakit.org/xforms/entities")
            ),
            head(
                title("Create entity form"),
                model(
                    mainInstance(
                        t("data id=\"create-entity-form\"",
                            t("name"),
                            t("orx:meta",
                                t("entities:entity dataset=\"people\"",
                                    t("entities:create")
                                )
                            )
                        )
                    ),
                    bind("/data/name").type("string")
                )
            ),
            body(
                input("/data/name")
            )
        );

        XFormParser parser = new XFormParser(new InputStreamReader(new ByteArrayInputStream(xForm.asXml().getBytes())));
        FormEntryModel formEntryModel = new FormEntryModel(parser.parse(null));

        EntityFormExtra entityFormExtra = new EntityFormExtra(asList(new Pair<>(new XPathReference("/data/name"), "name")));
        formEntryModel.getForm().getExtras().put(entityFormExtra);

        EntityFormFinalizationProcessor processor = new EntityFormFinalizationProcessor();
        processor.processForm(formEntryModel);

        Entity entity = formEntryModel.getExtras().get(Entities.class).getEntities().get(0);
        List<Pair<String, String>> properties = entity.properties;
        assertThat(properties.get(0).getSecond(), equalTo(""));
    }
}