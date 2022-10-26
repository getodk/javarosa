package org.javarosa.core.model.instance;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.javarosa.core.test.AnswerDataMatchers.stringAnswer;
import static org.javarosa.core.util.BindBuilderXFormsElement.bind;
import static org.javarosa.core.util.XFormsElement.body;
import static org.javarosa.core.util.XFormsElement.head;
import static org.javarosa.core.util.XFormsElement.html;
import static org.javarosa.core.util.XFormsElement.input;
import static org.javarosa.core.util.XFormsElement.mainInstance;
import static org.javarosa.core.util.XFormsElement.model;
import static org.javarosa.core.util.XFormsElement.t;
import static org.javarosa.core.util.XFormsElement.title;

import java.io.IOException;
import kotlin.Pair;
import org.javarosa.core.test.Scenario;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.xform.parse.XFormParser;
import org.junit.Test;

public class TreeElementNamespacedTest {

    @Test
    public void namespacedElement_canBeQueried() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("Namespaced element", html(
            asList(new Pair<>("example", "http://example.fake")),
            head(
                title("Namespaced element"),
                model(
                    mainInstance(t("data id=\"entity-creation\"",
                        t("question"),
                        t("example:calculate")
                    )),
                    bind("/data/example:calculate").calculate("/data/question")
                )
            ),
            body(
                input("/data/question")
            )));

        scenario.answer("/data/question", "foo");
        assertThat(scenario.answerOf("/data/example:calculate"), is(stringAnswer("foo")));
    }

    @Test
    public void namespacedElement_canBeQueried_afterSerializationDeserialization() throws IOException, XFormParser.ParseException, DeserializationException {
        Scenario scenario = Scenario.init("Namespaced element", html(
            asList(new Pair<>("example", "http://example.fake")),
            head(
                title("Namespaced element"),
                model(
                    mainInstance(t("data id=\"entity-creation\"",
                        t("question"),
                        t("example:calculate")
                    )),
                    bind("/data/example:calculate").calculate("/data/question")
                )
            ),
            body(
                input("/data/question")
            )));

        scenario.answer("/data/question", "foo");

        Scenario cachedScenario = scenario.serializeAndDeserializeForm();

        assertThat(cachedScenario.answerOf("/data/example:calculate"), is(stringAnswer("foo")));
    }
}
