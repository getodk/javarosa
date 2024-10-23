package org.javarosa.xpath.expr;

import org.javarosa.test.Scenario;
import org.javarosa.xform.parse.XFormParser;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

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

    @Test
    public void seedFromArbitraryInputCanBeUsed() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("Randomize non-numeric seed", html(
            head(
                title("Randomize non-numeric seed"),
                model(
                    mainInstance(t("data id=\"rand-non-numeric\"",
                        t("input"),
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
                    bind("/data/input").type("geopoint"),
                    bind("/data/choice").type("string")
                )
            ),
            body(
                input("/data/input"),
                select1Dynamic("/data/choice", "randomize(instance('choices')/root/item, /data/input)")
            )
        ));

        scenario.answer("/data/input", "-6.8137120026589315 39.29392995851879");
        String[] shuffled = {"h", "b", "d", "f", "a", "g", "c", "e"};
        for (int i = 0; i < shuffled.length; i++) {
            assertThat(scenario.choicesOf("/data/choice").get(i).getValue(), is(shuffled[i]));
        }
    }

    @Test
    public void seed0FromNaNs() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("Randomize non-numeric seed", html(
                head(
                        title("Randomize non-numeric seed"),
                        model(
                                mainInstance(t("data id=\"rand-non-numeric\"",
                                        t("input_emptystring"),
                                        t("input_somestring"),
                                        t("input_int"),
                                        t("choice_emptystring"),
                                        t("choice_somestring"),
                                        t("choice_int")
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
                                bind("/data/input_emptystring").type("string"),
                                bind("/data/input_somestring").type("string"),
                                bind("/data/input_int").type("int")
                        )
                ),
                body(
                        input("/data/input_emptystring"),
                        input("/data/input_somestring"),
                        input("/data/input_int"),
                        select1Dynamic("/data/choice_emptystring", "randomize(instance('choices')/root/item, /data/input_emptystring)"),
                        select1Dynamic("/data/choice_somestring", "randomize(instance('choices')/root/item, /data/input_somestring)"),
                        select1Dynamic("/data/choice_int", "randomize(instance('choices')/root/item, /data/input_int)")
                )
        ));

        scenario.answer("/data/input_emptystring", "");
        scenario.answer("/data/input_somestring", "somestring");
        scenario.answer("/data/input_int", "0");

        String[] shuffled_NaN_or_0 = {"c", "b", "h", "a", "f", "d", "g", "e"};
        String[] shuffled_somestring = {"e", "b", "c", "g", "d", "a", "f", "h"};
        assertThat("somestring-seeded expected order is distinct from 0-seeded expected order", !Arrays.equals(shuffled_NaN_or_0, shuffled_somestring));
        String[] shuffledfields = {"/data/choice_emptystring", "/data/choice_int"};
        for (int i = 0; i < shuffled_NaN_or_0.length; i++) {
            for (String shuffledfield : shuffledfields) {
                assertThat(scenario.choicesOf(shuffledfield).get(i).getValue(), is(shuffled_NaN_or_0[i]));
            }
        }
        for (int i = 0; i < shuffled_somestring.length; i++) {
            assertThat(scenario.choicesOf("/data/choice_somestring").get(i).getValue(), is(shuffled_somestring[i]));
        }
    }
}
