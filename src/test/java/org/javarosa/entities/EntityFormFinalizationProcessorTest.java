package org.javarosa.entities;

import org.javarosa.core.test.Scenario;
import org.javarosa.core.util.XFormsElement;
import org.javarosa.entities.internal.Entities;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.xform.parse.XFormParserFactory;
import org.javarosa.xform.util.XFormUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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

    private final EntityXFormParserFactory entityXFormParserFactory = new EntityXFormParserFactory(new XFormParserFactory());

    @Before
    public void setup() {
        XFormUtils.setXFormParserFactory(entityXFormParserFactory);
    }

    @After
    public void teardown() {
        XFormUtils.setXFormParserFactory(new XFormParserFactory());
    }

    @Test
    public void whenFormDoesNotHaveEntityElement_addsNoEntitiesToExtras() throws Exception {
        Scenario scenario = Scenario.init("Normal form", XFormsElement.html(
            head(
                title("Normal form"),
                model(
                    mainInstance(
                        t("data id=\"normal\"",
                            t("name")
                        )
                    ),
                    bind("/data/name").type("string")
                )
            ),
            body(
                input("/data/name")
            )
        ));

        EntityFormFinalizationProcessor processor = new EntityFormFinalizationProcessor();
        FormEntryModel model = scenario.getFormEntryController().getModel();
        processor.processForm(model);
        assertThat(model.getExtras().get(Entities.class), equalTo(null));
    }
}