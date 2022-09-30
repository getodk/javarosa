package org.javarosa.entities;

import kotlin.Pair;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.util.XFormsElement;
import org.javarosa.entities.internal.EntityDatasetParser;
import org.javarosa.xform.util.XFormUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;

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

public class EntityDatasetParserTest {

    @Test
    public void parseFirstDatasetToCreate_ignoresDatasetWithCreateActionWithIncorrectNamespace() {
        XFormsElement form = XFormsElement.html(
            asList(
                new Pair<>("correct", "http://www.opendatakit.org/xforms/entities"),
                new Pair<>("incorrect", "blah")
            ),
            head(
                title("Create entity form"),
                model(
                    mainInstance(
                        t("data id=\"create-entity-form\"",
                            t("name"),
                            t("orx:meta",
                                t("correct:entity dataset=\"people\"",
                                    t("incorrect:create")
                                )
                            )
                        )
                    ),
                    bind("/data/name").type("string").withAttribute("correct", "saveto", "name")
                )
            ),
            body(
                input("/data/name")
            )
        );

        FormDef formDef = XFormUtils.getFormFromInputStream(new ByteArrayInputStream(form.asXml().getBytes()));
        FormInstance mainInstance = formDef.getMainInstance();

        String dataset = EntityDatasetParser.parseFirstDatasetToCreate(mainInstance);
        assertThat(dataset, equalTo(null));
    }
}