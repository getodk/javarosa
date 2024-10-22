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
import static org.javarosa.test.XFormsElement.repeat;
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
                        t("choices_numeric_seed"),
                        t("choices_stringified_numeric_seed")
                    )),
                    instance("choices",
                        item("a", "A"),
                        item("b", "B"),
                        item("c", "C"),
                        item("d", "D"),
                        item("e", "E"),
                        item("f", "F"),
                        item("g", "G"),
                        item("h", "H")
                    ),
                    bind("/data/choices_numeric_seed").type("string"),
                    bind("/data/choices_stringified_numeric_seed").type("string")
                )
            ),
            body(
                select1Dynamic("/data/choices_numeric_seed", "randomize(instance('choices')/root/item, 1234)"),
                select1Dynamic("/data/choices_stringified_numeric_seed", "randomize(instance('choices')/root/item, '1234')")
            )
        ));
        String[] shuffled = {"g", "f", "e", "d", "a", "h", "b", "c"};
        String[] nodes = {"/data/choices_numeric_seed", "/data/choices_stringified_numeric_seed"};
        for (int i = 0; i < shuffled.length; i++) {
            for (String node : nodes) {
                assertThat(scenario.choicesOf(node).get(i).getValue(), is(shuffled[i]));
            }
        }
    }

    @Test
    public void stringNumberSeedConvertsWhenUsedInCalculate() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("Randomize non-numeric seed", html(
            head(
                title("Randomize non-numeric seed"),
                model(
                    mainInstance(t("data id=\"rand-non-numeric\"",
                        t("choices_numeric_seed"),
                        t("choices_stringified_numeric_seed")
                    )),
                    instance("choices",
                        item("a", "A"),
                        item("b", "B"),
                        item("c", "C"),
                        item("d", "D"),
                        item("e", "E"),
                        item("f", "F"),
                        item("g", "G"),
                        item("h", "H")
                    ),
                    bind("/data/choices_numeric_seed").type("string").calculate("join('', randomize(instance('choices')/root/item/label, 1234))"),
                    bind("/data/choices_stringified_numeric_seed").type("string").calculate("join('', randomize(instance('choices')/root/item/label, '1234'))")
                )
            ),
            body(
                input("/data/choices_numeric_seed"),
                input("/data/choices_stringified_numeric_seed")
            )
        ));
        assertThat(scenario.answerOf("/data/choices_numeric_seed").getDisplayText(), is(scenario.answerOf("/data/choices_stringified_numeric_seed").getDisplayText()));
        assertThat(scenario.answerOf("/data/choices_numeric_seed").getDisplayText(), is("GFEDAHBC"));
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
                        item("b", "B"),
                        item("c", "C"),
                        item("d", "D"),
                        item("e", "E"),
                        item("f", "F"),
                        item("g", "G"),
                        item("h", "H")
                    ),
                    bind("/data/choice").type("string")
                )
            ),
            body(
                select1Dynamic("/data/choice", "randomize(instance('choices')/root/item, 'foo')")
            )
        ));
        String[] shuffled = {"e", "a", "d", "b", "h", "g", "c", "f"};
        for (int i = 0; i < shuffled.length; i++) {
            assertThat(scenario.choicesOf("/data/choice").get(i).getValue(), is(shuffled[i]));
        }
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
                        item("b", "B"),
                        item("c", "C"),
                        item("d", "D"),
                        item("e", "E"),
                        item("f", "F"),
                        item("g", "G"),
                        item("h", "H")
                    ),
                    bind("/data/choice").type("string").calculate("join('', randomize(instance('choices')/root/item/label, 'foo'))")
                )
            ),
            body(
                input("/data/choice")
            )
        ));

        assertThat(scenario.answerOf("/data/choice").getDisplayText(), is("EADBHGCF"));
    }

    @Test
    public void seedInRepeatIsEvaluatedForEachInstance() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("Randomize non-numeric seed", html(
            head(
                title("Randomize non-numeric seed"),
                model(
                    mainInstance(t("data id=\"rand-non-numeric\"",
                        t("repeat",
                            t("input"),
                            t("choice")
                        )
                    )),
                    instance("choices",
                        item("a", "A"),
                        item("b", "B")
                    ),
                    bind("/data/input").type("string"),
                    bind("/data/choice").type("string")
                )
            ),
            body(
                repeat("/data/repeat",
                    input("/data/repeat/input"),
                    select1Dynamic("/data/repeat/choice", "randomize(instance('choices')/root/item, 1 * ../input)")
                )
            )
        ));

        scenario.answer("/data/repeat[1]/input", 0);
        assertThat(scenario.choicesOf("/data/repeat[1]/choice").get(0).getValue(), is("a"));

        scenario.createNewRepeat("/data/repeat");
        scenario.answer("/data/repeat[2]/input", 1);
        assertThat(scenario.choicesOf("/data/repeat[2]/choice").get(0).getValue(), is("b"));
        assertThat(scenario.choicesOf("/data/repeat[1]/choice").get(0).getValue(), is("a"));
    }
}
