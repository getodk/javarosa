package org.javarosa.core.model.utils.test;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
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
import org.javarosa.core.test.Scenario;
import org.javarosa.xform.parse.XFormParser;
import org.junit.Ignore;
import org.junit.Test;

public class QuestionPreloaderTest {
    @Ignore("Not supported")
    @Test
    public void preloader_preloadsAttributes() throws IOException, XFormParser.ParseException {
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

        assertThat(scenario.answerOf("/data/element/@attr").getDisplayText(), startsWith("uuid"));
    }
}
