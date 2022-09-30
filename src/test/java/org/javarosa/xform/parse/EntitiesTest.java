package org.javarosa.xform.parse;

import kotlin.Pair;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.test.Scenario;
import org.javarosa.core.util.XFormsElement;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.entities.Entity;
import org.javarosa.entities.EntityFormPostProcessor;
import org.javarosa.entities.EntityXFormParserFactory;
import org.javarosa.entities.internal.Entities;
import org.javarosa.xform.util.XFormUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.javarosa.core.util.BindBuilderXFormsElement.bind;
import static org.javarosa.core.util.XFormsElement.body;
import static org.javarosa.core.util.XFormsElement.head;
import static org.javarosa.core.util.XFormsElement.input;
import static org.javarosa.core.util.XFormsElement.item;
import static org.javarosa.core.util.XFormsElement.mainInstance;
import static org.javarosa.core.util.XFormsElement.model;
import static org.javarosa.core.util.XFormsElement.select1;
import static org.javarosa.core.util.XFormsElement.t;
import static org.javarosa.core.util.XFormsElement.title;

public class EntitiesTest {

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
    public void fillingFormWithoutCreate_doesNotCreateAnyEntities() throws IOException {
        Scenario scenario = Scenario.init("Entity form", XFormsElement.html(
            asList(
                new Pair<>("entities", "http://www.opendatakit.org/xforms/entities")
            ),
            head(
                title("Entity form"),
                model(
                    mainInstance(
                        t("data id=\"entity-form\"",
                            t("name"),
                            t("orx:meta",
                                t("entities:entity dataset=\"people\"")
                            )
                        )
                    ),
                    bind("/data/name").type("string").withAttribute("entities", "saveto", "name")
                )
            ),
            body(
                input("/data/name")
            )
        ));

        scenario.getFormEntryController().addPostProcessor(new EntityFormPostProcessor());

        scenario.next();
        scenario.answer("Tom Wambsgans");

        scenario.finalizeInstance();
        List<Entity> entities = scenario.getFormEntryController().getModel().getExtras().get(Entities.class).getEntities();
        assertThat(entities.size(), equalTo(0));
    }

    @Test
    public void fillingFormWithCreate_makesEntityAvailable() throws IOException {
        Scenario scenario = Scenario.init("Create entity form", XFormsElement.html(
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
                    bind("/data/name").type("string").withAttribute("entities", "saveto", "name")
                )
            ),
            body(
                input("/data/name")
            )
        ));

        scenario.getFormEntryController().addPostProcessor(new EntityFormPostProcessor());

        scenario.next();
        scenario.answer("Tom Wambsgans");

        scenario.finalizeInstance();
        List<Entity> entities = scenario.getFormEntryController().getModel().getExtras().get(Entities.class).getEntities();
        assertThat(entities.size(), equalTo(1));
        assertThat(entities.get(0).dataset, equalTo("people"));
        assertThat(entities.get(0).fields, equalTo(asList(new Pair<>("name", "Tom Wambsgans"))));
    }

    @Test
    public void fillingFormWithNonRelevantCreate_doesNotCreateAnyEntities() throws IOException {
        Scenario scenario = Scenario.init("Create entity form", XFormsElement.html(
            asList(
                new Pair<>("entities", "http://www.opendatakit.org/xforms/entities")
            ),
            head(
                title("Create entity form"),
                model(
                    mainInstance(
                        t("data id=\"create-entity-form\"",
                            t("name"),
                            t("join"),
                            t("orx:meta",
                                t("entities:entity dataset=\"members\"",
                                    t("entities:create")
                                )
                            )
                        )
                    ),
                    bind("/data/orx:meta/entities:entity/entities:create").relevant("/data/join = 'yes'"),
                    bind("/data/name").type("string").withAttribute("entities", "saveto", "name")
                )
            ),
            body(
                input("/data/name"),
                select1("/data/join", item("yes", "Yes"), item("no", "No"))
            )
        ));

        scenario.getFormEntryController().addPostProcessor(new EntityFormPostProcessor());

        scenario.next();
        scenario.answer("Roman Roy");
        scenario.answer(scenario.choicesOf("/data/join").get(1));

        scenario.finalizeInstance();
        List<Entity> entities = scenario.getFormEntryController().getModel().getExtras().get(Entities.class).getEntities();
        assertThat(entities.size(), equalTo(0));
    }

    @Test
    public void entityFormCanBeSerialized() throws IOException, DeserializationException {
        Scenario scenario = Scenario.init("Create entity form", XFormsElement.html(
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
                    bind("/data/name").type("string").withAttribute("entities", "saveto", "name")
                )
            ),
            body(
                input("/data/name")
            )
        ));

        scenario.getFormEntryController().addPostProcessor(new EntityFormPostProcessor());

        Scenario deserializedScenario = scenario.serializeAndDeserializeForm();
        deserializedScenario.getFormEntryController().addPostProcessor(new EntityFormPostProcessor());

        deserializedScenario.next();
        deserializedScenario.answer("Shiv Roy");

        deserializedScenario.finalizeInstance();
        List<Entity> entities = deserializedScenario.getFormEntryController().getModel().getExtras().get(Entities.class).getEntities();
        assertThat(entities.size(), equalTo(1));
        assertThat(entities.get(0).dataset, equalTo("people"));
        assertThat(entities.get(0).fields, equalTo(asList(new Pair<>("name", "Shiv Roy"))));
    }

    @Test
    public void entitiesNamespaceWorksRegardlessOfName() throws IOException, DeserializationException {
        Scenario scenario = Scenario.init("Create entity form", XFormsElement.html(
            asList(
                new Pair<>("blah", "http://www.opendatakit.org/xforms/entities")
            ),
            head(
                title("Create entity form"),
                model(
                    mainInstance(
                        t("data id=\"create-entity-form\"",
                            t("name"),
                            t("orx:meta",
                                t("blah:entity dataset=\"people\"",
                                    t("blah:create")
                                )
                            )
                        )
                    ),
                    bind("/data/name").type("string").withAttribute("blah", "saveto", "name")
                )
            ),
            body(
                input("/data/name")
            )
        ));

        scenario.getFormEntryController().addPostProcessor(new EntityFormPostProcessor());

        scenario.next();
        scenario.answer("Tom Wambsgans");

        scenario.finalizeInstance();
        List<Entity> entities = scenario.getFormEntryController().getModel().getExtras().get(Entities.class).getEntities();
        assertThat(entities.size(), equalTo(1));
        assertThat(entities.get(0).fields, equalTo(asList(new Pair<>("name", "Tom Wambsgans"))));
    }

    @Test
    public void mustUseCorrectNamespace() throws IOException {
        Scenario scenario = Scenario.init("Create entity form", XFormsElement.html(
            asList(
                new Pair<>("entities", "http://www.example.com/xforms/entities")
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
                    bind("/data/name").type("string").withAttribute("entities", "saveto", "name")
                )
            ),
            body(
                input("/data/name")
            )
        ));

        scenario.getFormEntryController().addPostProcessor(new EntityFormPostProcessor());

        scenario.next();
        scenario.answer("Tom Wambsgans");

        scenario.finalizeInstance();
        List<Entity> entities = scenario.getFormEntryController().getModel().getExtras().get(Entities.class).getEntities();
        assertThat(entities.size(), equalTo(0));
    }

    @Test
    public void fillingFormWithSelectSaveTo_andWithCreate_savesValuesCorrectlyToEntity() throws IOException {
        Scenario scenario = Scenario.init("Create entity form", XFormsElement.html(
            asList(
                new Pair<>("entities", "http://www.opendatakit.org/xforms/entities")
            ),
            head(
                title("Create entity form"),
                model(
                    mainInstance(
                        t("data id=\"create-entity-form\"",
                            t("team"),
                            t("orx:meta",
                                t("entities:entity dataset=\"people\"",
                                    t("entities:create")
                                )
                            )
                        )
                    ),
                    bind("/data/team").type("string").withAttribute("entities", "saveto", "team")
                )
            ),
            body(
                select1("/data/team", item("kendall", "Kendall"), item("logan", "Logan"))
            )
        ));

        scenario.getFormEntryController().addPostProcessor(new EntityFormPostProcessor());

        scenario.next();
        scenario.answer(scenario.choicesOf("/data/team").get(0));

        scenario.finalizeInstance();
        List<Entity> entities = scenario.getFormEntryController().getModel().getExtras().get(Entities.class).getEntities();
        assertThat(entities.size(), equalTo(1));
        assertThat(entities.get(0).fields, equalTo(asList(new Pair<>("team", "kendall"))));
    }

    @Test
    public void savetoIsRemovedFromBindAttributesForClients() throws IOException {
        Scenario scenario = Scenario.init("Create entity form", XFormsElement.html(
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
                    bind("/data/name").type("string").withAttribute("entities", "saveto", "name")
                )
            ),
            body(
                input("/data/name")
            )
        ));

        scenario.next();
        List<TreeElement> bindAttributes = scenario.getFormEntryPromptAtIndex().getBindAttributes();
        boolean containsSaveTo = bindAttributes.stream().anyMatch(treeElement -> treeElement.getName().equals("saveto"));
        assertThat(containsSaveTo, is(false));
    }
}
