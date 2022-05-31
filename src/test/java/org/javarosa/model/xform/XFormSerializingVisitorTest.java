package org.javarosa.model.xform;

import org.javarosa.core.test.Scenario;
import org.javarosa.core.util.XFormsElement;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.javarosa.core.util.BindBuilderXFormsElement.bind;
import static org.javarosa.core.util.XFormsElement.body;
import static org.javarosa.core.util.XFormsElement.head;
import static org.javarosa.core.util.XFormsElement.html;
import static org.javarosa.core.util.XFormsElement.input;
import static org.javarosa.core.util.XFormsElement.mainInstance;
import static org.javarosa.core.util.XFormsElement.model;
import static org.javarosa.core.util.XFormsElement.t;
import static org.javarosa.core.util.XFormsElement.title;

public class XFormSerializingVisitorTest {

    @Test
    public void serializeInstance_preservesUnicodeCharacters() throws IOException {
        XFormsElement formDef = html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("text")
                    )),
                    bind("/data/text").type("string")
                )
            ),
            body(input("/data/text"))
        );

        Scenario scenario = Scenario.init("Some form", formDef);
        scenario.next();
        scenario.answer("\uD83E\uDDDB");

        XFormSerializingVisitor visitor = new XFormSerializingVisitor();
        byte[] serializedInstance = visitor.serializeInstance(scenario.getFormDef().getMainInstance());
        assertThat(new String(serializedInstance), containsString("<text>\uD83E\uDDDB</text>"));
    }
}