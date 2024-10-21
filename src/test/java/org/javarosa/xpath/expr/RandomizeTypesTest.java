package org.javarosa.xpath.expr;

import org.javarosa.test.Scenario;
import org.javarosa.xform.parse.XFormParser;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.javarosa.test.BindBuilderXFormsElement.bind;
import static org.javarosa.test.XFormsElement.body;
import static org.javarosa.test.XFormsElement.head;
import static org.javarosa.test.XFormsElement.html;
import static org.javarosa.test.XFormsElement.input;
import static org.javarosa.test.XFormsElement.instance;
import static org.javarosa.test.XFormsElement.item;
import static org.javarosa.test.XFormsElement.mainInstance;
import static org.javarosa.test.XFormsElement.model;
import static org.javarosa.test.XFormsElement.select1Dynamic;
import static org.javarosa.test.XFormsElement.t;
import static org.javarosa.test.XFormsElement.title;

public class RandomizeTypesTest {
    @Test
    public void stringNumberSeedConvertsWhenUsedInNodesetExpression() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("Randomize non-numeric seed", html(
            head(
                title("Randomize non-numeric seed"),
                model(
                    mainInstance(t("data id=\"rand-non-numeric\"",
                        t("choice")
                    )),
                    instance("choices",
                        item("a", "A"),
                        item("b", "B")
                    ),
                    bind("/data/choice").type("string")
                )
            ),
            body(
                select1Dynamic("/data/choice", "randomize(instance('choices')/root/item, '1')")
            )
        ));

        assertThat(scenario.choicesOf("/data/choice").get(0).getValue(), is("b"));
    }

    @Test
    public void stringNumberSeedConvertsWhenUsedInCalculate() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("Randomize non-numeric seed", html(
            head(
                title("Randomize non-numeric seed"),
                model(
                    mainInstance(t("data id=\"rand-non-numeric\"",
                        t("choice")
                    )),
                    instance("choices",
                        item("a", "A"),
                        item("b", "B")
                    ),
                    bind("/data/choice").type("string").calculate("selected-at(join(' ', randomize(instance('choices')/root/item/label, '1')), 0)")
                )
            ),
            body(
                input("/data/choice")
            )
        ));

        assertThat(scenario.answerOf("/data/choice").getDisplayText(), is("B"));
    }

    @Test
    public void stringTextSeedConvertsWhenUsedInNodesetExpression() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("Randomize non-numeric seed", html(
            head(
                title("Randomize non-numeric seed"),
                model(
                    mainInstance(t("data id=\"rand-non-numeric\"",
                        t("choice")
                    )),
                    instance("choices",
                        item("a", "A"),
                        item("b", "B")
                    ),
                    bind("/data/choice").type("string")
                )
            ),
            body(
                select1Dynamic("/data/choice", "randomize(instance('choices')/root/item, 'foo')")
            )
        ));

        assertThat(scenario.choicesOf("/data/choice").get(0).getValue(), is("b"));
    }

    @Test
    public void stringTextSeedConvertsWhenUsedInCalculate() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("Randomize non-numeric seed", html(
            head(
                title("Randomize non-numeric seed"),
                model(
                    mainInstance(t("data id=\"rand-non-numeric\"",
                        t("choice")
                    )),
                    instance("choices",
                        item("a", "A"),
                        item("b", "B")
                    ),
                    bind("/data/choice").type("string").calculate("selected-at(join(' ', randomize(instance('choices')/root/item/label, 'foo')), 0)")
                )
            ),
            body(
                input("/data/choice")
            )
        ));

        assertThat(scenario.answerOf("/data/choice").getDisplayText(), is("B"));
    }
}
