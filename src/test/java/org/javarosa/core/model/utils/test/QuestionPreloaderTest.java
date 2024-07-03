package org.javarosa.core.model.utils.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.javarosa.test.BindBuilderXFormsElement.bind;
import static org.javarosa.test.XFormsElement.body;
import static org.javarosa.test.XFormsElement.head;
import static org.javarosa.test.XFormsElement.html;
import static org.javarosa.test.XFormsElement.input;
import static org.javarosa.test.XFormsElement.mainInstance;
import static org.javarosa.test.XFormsElement.model;
import static org.javarosa.test.XFormsElement.t;
import static org.javarosa.test.XFormsElement.title;

import java.io.IOException;
import org.javarosa.test.Scenario;
import org.javarosa.xform.parse.XFormParser;
import org.junit.Test;

public class QuestionPreloaderTest {
    @Test
    public void preloader_preloadsElements() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("Preload attribute", html(
            head(
                title("Preload element"),
                model(
                    mainInstance(t("data id=\"preload-attribute\"",
                        t("element")
                    )),
                    bind("/data/element").preload("uid")
                )
            ),
            body(
                input("/data/element")
            )));

        assertThat(scenario.answerOf("/data/element").getDisplayText(), startsWith("uuid:"));
    }

    @Test
    // Unintentional limitation
    public void preloader_doesNotpreloadAttributes() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("Preload attribute", html(
            head(
                title("Preload attribute"),
                model(
                    mainInstance(t("data id=\"preload-attribute\"",
                        t("element attr=\"\"")
                    )),
                    bind("/data/element/@attr").preload("uid")
                )
            ),
            body(
                input("/data/element")
            )));

        assertThat(scenario.answerOf("/data/element/@attr").getDisplayText(), is(""));
    }
}
