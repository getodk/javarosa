package org.javarosa.core.model.instance;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.nullValue;
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
import org.javarosa.core.test.Scenario;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.junit.Test;

public class TreeReferenceSerializationTest {
    @Test
    public void mainInstanceName_afterDeserialization_isNull() throws IOException, DeserializationException {
        Scenario scenario = Scenario.init("Tree reference deserialization", html(
            head(
                title("Tree reference deserialization"),
                model(
                    mainInstance(t("data id=\"treeref-deserialization\"",
                        t("a")
                    )),
                    bind("/data/a").type("string")
                )
            ),
            body(
                input("/data/a")
            )));

        scenario.next();
        assertThat(scenario.refAtIndex().getInstanceName(), is(nullValue()));
        assertThat(scenario.getFormDef().getEvaluationContext().getContextRef().toString(), is("/"));
        assertThat(scenario.getFormDef().getMainInstance().getBase().getInstanceName(), is(nullValue()));

        Scenario deserialized = scenario.serializeAndDeserializeForm();

        deserialized.next();
        assertThat(deserialized.refAtIndex().getInstanceName(), is(nullValue()));

        // Most contextualization needs are met by using the /context ref/ from the evaluation context.
        assertThat(deserialized.getFormDef().getEvaluationContext().getContextRef().toString(), is("/"));

        // Constraint evaluation uses XPathPathExprEval.eval which uses the /instance/ from the evaluation context to get a
        // TreeReference. Then XPathPathExpr.getRefValue sees whether that reference is the same as the latest modified
        // question by using TreeRefence.equals.
        assertThat(deserialized.getFormDef().getMainInstance().getBase().getInstanceName(), is(nullValue()));
    }

    @Test
    public void constraintsAreCorrectlyApplied_afterDeserialization() throws IOException, DeserializationException {
        Scenario scenario = Scenario.init("Tree reference deserialization", html(
            head(
                title("Tree reference deserialization"),
                model(
                    mainInstance(t("data id=\"treeref-deserialization\"",
                        t("a", "not ok"),
                        t("b")
                    )),
                    bind("/data/a").type("string"),
                    bind("/data/b").type("string").constraint(". != /data/a")
                )
            ),
            body(
                input("/data/b")
            )));

        scenario.next();
        scenario.answer("ok");
        assertThat(scenario.answerOf("/data/b[0]"), is(stringAnswer("ok")));

        scenario.answer("not ok");
        assertThat(scenario.answerOf("/data/b[0]"), is(stringAnswer("ok")));

        Scenario deserialized = scenario.serializeAndDeserializeForm();

        deserialized.next();
        deserialized.answer("ok");
        assertThat(deserialized.answerOf("/data/b[0]"), is(stringAnswer("ok")));

        deserialized.answer("not ok");
        assertThat(deserialized.answerOf("/data/b[0]"), is(stringAnswer("ok")));
    }
}
