package org.javarosa.entities.internal;

import kotlin.Pair;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.util.XFormsElement;
import org.javarosa.entities.UnrecognizedEntityVersionException;
import org.javarosa.xform.parse.XFormParser;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.javarosa.core.util.BindBuilderXFormsElement.bind;
import static org.javarosa.core.util.XFormsElement.body;
import static org.javarosa.core.util.XFormsElement.head;
import static org.javarosa.core.util.XFormsElement.input;
import static org.javarosa.core.util.XFormsElement.mainInstance;
import static org.javarosa.core.util.XFormsElement.model;
import static org.javarosa.core.util.XFormsElement.t;
import static org.javarosa.core.util.XFormsElement.title;

public class EntityFormParseProcessorTest {

    @Test(expected = UnrecognizedEntityVersionException.class)
    public void whenVersionIsNotRecognized_throwsException() throws IOException {
        XFormsElement form = XFormsElement.html(
            asList(
                new Pair<>("entities", "http://www.opendatakit.org/xforms/entities")
            ),
            head(
                title("Create entity form"),
                t("model entities:entities-version=\"somethingElse\"",
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
                    bind("/data/name").type("string").withAttribute("entities", "saveto", "name")
                )
            ),
            body(
                input("/data/name")
            )
        );

        EntityFormParseProcessor processor = new EntityFormParseProcessor();

        XFormParser parser = new XFormParser(new InputStreamReader(new ByteArrayInputStream(form.asXml().getBytes())));
        parser.addProcessor(processor);
        parser.parse(null);
    }

    @Test
    public void whenVersionIsNewPatch_doesNotThrowException() throws IOException {
        String newPatchVersion = EntityFormParseProcessor.SUPPORTED_VERSION + ".12";

        XFormsElement form = XFormsElement.html(
            asList(
                new Pair<>("entities", "http://www.opendatakit.org/xforms/entities")
            ),
            head(
                title("Create entity form"),
                t("model entities:entities-version=\"" + newPatchVersion + "\"",
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
                    bind("/data/name").type("string").withAttribute("entities", "saveto", "name")
                )
            ),
            body(
                input("/data/name")
            )
        );

        EntityFormParseProcessor processor = new EntityFormParseProcessor();

        XFormParser parser = new XFormParser(new InputStreamReader(new ByteArrayInputStream(form.asXml().getBytes())));
        parser.addProcessor(processor);
        parser.parse(null);
    }

    @Test
    public void saveTosWithIncorrectNamespaceAreIgnored() throws IOException {
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
                                    t("correct:create")
                                )
                            )
                        )
                    ),
                    bind("/data/name").type("string").withAttribute("incorrect", "saveto", "name")
                )
            ),
            body(
                input("/data/name")
            )
        );

        EntityFormParseProcessor processor = new EntityFormParseProcessor();

        XFormParser parser = new XFormParser(new InputStreamReader(new ByteArrayInputStream(form.asXml().getBytes())));
        parser.addProcessor(processor);

        FormDef formDef = parser.parse(null);
        assertThat(formDef.getExtras().get(EntityFormExtra.class).getSaveTos(), is(empty()));
    }
}