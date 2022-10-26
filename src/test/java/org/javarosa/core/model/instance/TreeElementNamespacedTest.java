package org.javarosa.core.model.instance;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.javarosa.core.model.instance.TreeReference.INDEX_ATTRIBUTE;
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

    @Test
    public void getAttribute_getsNamespacedAttribute() {
        TreeElement e1 = new TreeElement("a", INDEX_ATTRIBUTE);
        TreeElement e2 = new TreeElement("a", INDEX_ATTRIBUTE);
        e2.setNamespace("https://fake.fake");
        e2.setNamespacePrefix("example");
        TreeElement result = TreeElement.getAttribute(asList(e1, e2), "https://fake.fake", "a");

        assertThat(result, is(e2));
    }

    @Test
    // This is what happens when evaluating an XPath expression
    public void getAttribute_getsNamespacedAttribute_usingPrefix() {
        TreeElement e1 = new TreeElement("a", INDEX_ATTRIBUTE);
        TreeElement e2 = new TreeElement("a", INDEX_ATTRIBUTE);
        e2.setNamespace("https://fake.fake");
        e2.setNamespacePrefix("example");
        TreeElement result = TreeElement.getAttribute(asList(e1, e2), null, "example:a");

        assertThat(result, is(e2));
    }

    @Test
    public void getAttribute_getsDefaultNamespaceAttribute() {
        TreeElement e1 = new TreeElement("a", INDEX_ATTRIBUTE);
        e1.setNamespace("https://fake.fake");
        e1.setNamespacePrefix("example");
        TreeElement e2 = new TreeElement("a", INDEX_ATTRIBUTE);
        TreeElement result = TreeElement.getAttribute(asList(e1, e2), null, "a");

        assertThat(result, is(e2));
    }

    @Test
    // Attributes in the main instance without a custom namespace have empty string namespace
    public void getAttribute_getsDefaultNamespaceAttribute_withBlankNamespace() {
        TreeElement e1 = new TreeElement("a", INDEX_ATTRIBUTE);
        e1.setNamespace("https://fake.fake");
        e1.setNamespacePrefix("example");
        TreeElement e2 = new TreeElement("a", INDEX_ATTRIBUTE);
        e2.setNamespace("");
        TreeElement result = TreeElement.getAttribute(asList(e1, e2), null, "a");

        assertThat(result, is(e2));
    }
}
