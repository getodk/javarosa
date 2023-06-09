package org.javarosa.xform.parse;

import kotlin.Pair;
import org.javarosa.core.model.DataBinding;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.util.XFormsElement;
import org.javarosa.model.xform.XPathReference;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

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

public class BindAttributeProcessorTest {

    @Test
    public void doesNotProcessAttributeWithIncorrectNamespace() throws IOException, ParseException {
        XFormsElement form = XFormsElement.html(
            asList(
                new Pair<>("blah", "blah"),
                new Pair<>("notBlah", "notBlah")
            ),
            head(
                title("Form"),
                model(
                    mainInstance(
                        t("data id=\"form\"",
                            t("name")
                        )
                    ),
                    bind("/data/name").type("string").withAttribute("notBlah", "name", "value")
                )
            ),
            body(
                input("/data/name")
            )
        );

        XFormParser parser = new XFormParser(new InputStreamReader(new ByteArrayInputStream(form.asXml().getBytes())));

        RecordingBindAttributeProcessor processor = new RecordingBindAttributeProcessor(new HashSet<>(asList(new Pair<>("blah", "name"))));
        parser.addBindAttributeProcessor(processor);

        parser.parse();
        assertThat(processor.processCalled, equalTo(false));
    }
    @Test
    public void doesNotRemovettributeWithIncorrectNamespace() throws IOException, ParseException {
        XFormsElement form = XFormsElement.html(
            asList(
                new Pair<>("blah", "blah"),
                new Pair<>("notBlah", "notBlah")
            ),
            head(
                title("Form"),
                model(
                    mainInstance(
                        t("data id=\"form\"",
                            t("name")
                        )
                    ),
                    bind("/data/name").type("string").withAttribute("notBlah", "name", "value")
                )
            ),
            body(
                input("/data/name")
            )
        );

        XFormParser parser = new XFormParser(new InputStreamReader(new ByteArrayInputStream(form.asXml().getBytes())));

        RecordingBindAttributeProcessor processor = new RecordingBindAttributeProcessor(new HashSet<>(asList(new Pair<>("blah", "name"))));
        parser.addBindAttributeProcessor(processor);

        FormDef formDef = parser.parse();

        TreeReference questionRef = XPathReference.getPathExpr("/data/name").getReference();
        TreeElement questionElement = formDef.getMainInstance().resolveReference(questionRef);
        assertThat(questionElement.getBindAttributes().size(), equalTo(1));
    }

    private static class RecordingBindAttributeProcessor implements XFormParser.BindAttributeProcessor {

        private final Set<Pair<String, String>> attributes;
        boolean processCalled;

        public RecordingBindAttributeProcessor(Set<Pair<String, String>> attributes) {
            this.attributes = attributes;
        }

        @Override
        public Set<Pair<String, String>> getBindAttributes() {
            return attributes;
        }

        @Override
        public void processBindAttribute(String name, String value, DataBinding binding) {
            processCalled = true;
        }
    }
}
