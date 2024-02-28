package org.javarosa.core.model;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.javarosa.core.util.BindBuilderXFormsElement.bind;
import static org.javarosa.core.util.XFormsElement.body;
import static org.javarosa.core.util.XFormsElement.head;
import static org.javarosa.core.util.XFormsElement.html;
import static org.javarosa.core.util.XFormsElement.input;
import static org.javarosa.core.util.XFormsElement.instance;
import static org.javarosa.core.util.XFormsElement.item;
import static org.javarosa.core.util.XFormsElement.mainInstance;
import static org.javarosa.core.util.XFormsElement.model;
import static org.javarosa.core.util.XFormsElement.repeat;
import static org.javarosa.core.util.XFormsElement.select1Dynamic;
import static org.javarosa.core.util.XFormsElement.t;
import static org.javarosa.core.util.XFormsElement.title;

import org.javarosa.core.test.Scenario;
import org.javarosa.measure.Measure;
import org.junit.Test;

public class SelectCachingTest {

    @Test
    public void eqChoiceFiltersAreOnlyEvaluatedOnceForRepeatedChoiceListEvaluations() throws Exception {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("choice"),
                        t("select")
                    )),
                    instance("instance",
                        item("a", "A"),
                        item("b", "B")
                    ),
                    bind("/data/choice").type("string"),
                    bind("/data/select").type("string")
                )
            ),
            body(
                input("/data/choice"),
                select1Dynamic("/data/select", "instance('instance')/root/item[value=/data/choice]")
            )
        ));

        int evaluations = Measure.withMeasure(asList("PredicateEvaluation", "IndexEvaluation"), () -> {
            scenario.answer("/data/choice", "a");

            scenario.choicesOf("/data/select");
            scenario.choicesOf("/data/select");
        });

        // Check that we do just (size of secondary instance)
        assertThat(evaluations, equalTo(2));
    }

    @Test
    public void andOfTwoEqChoiceFiltersAreOnlyEvaluatedOnceForRepeatedChoiceListEvaluations() throws Exception {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("choice"),
                        t("select")
                    )),
                    instance("instance",
                        item("a", "A"),
                        item("b", "B")
                    ),
                    bind("/data/choice").type("string"),
                    bind("/data/select").type("string")
                )
            ),
            body(
                input("/data/choice"),
                select1Dynamic("/data/select", "instance('instance')/root/item[value=/data/choice and value=/data/choice]")
            )
        ));

        int evaluations = Measure.withMeasure(asList("PredicateEvaluation", "IndexEvaluation"), () -> {
            scenario.answer("/data/choice", "a");

            scenario.choicesOf("/data/select");
            scenario.choicesOf("/data/select");
        });

        // Check that we do just (size of secondary instance)
        assertThat(evaluations, equalTo(2));
    }

    @Test
    public void andOfTwoEqChoiceFiltersIsNotConfusedWithOr() throws Exception {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("choice"),
                        t("select1"),
                        t("select2")
                    )),
                    instance("instance",
                        item("a", "A"),
                        item("b", "B")
                    ),
                    bind("/data/choice").type("string"),
                    bind("/data/select1").type("string"),
                    bind("/data/select2").type("string")
                )
            ),
            body(
                input("/data/choice"),
                select1Dynamic("/data/select1", "instance('instance')/root/item[value=/data/choice or value!=/data/choice]"),
                select1Dynamic("/data/select2", "instance('instance')/root/item[value=/data/choice and value!=/data/choice]")
            )
        ));

        scenario.answer("/data/choice", "a");
        assertThat(scenario.choicesOf("/data/select1").size(), equalTo(2));
        assertThat(scenario.choicesOf("/data/select2").size(), equalTo(0));
    }

    @Test
    public void repeatedEqChoiceFiltersAreOnlyEvaluatedOnce_whileLiteralExpressionIsTheSame() throws Exception {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("choice"),
                        t("select1"),
                        t("select2")
                    )),
                    instance("instance",
                        item("a", "A"),
                        item("b", "B")
                    ),
                    bind("/data/choice").type("string"),
                    bind("/data/select1").type("string"),
                    bind("/data/select2").type("string")
                )
            ),
            body(
                input("/data/choice"),
                select1Dynamic("/data/select1", "instance('instance')/root/item[value=/data/choice]"),
                select1Dynamic("/data/select2", "instance('instance')/root/item[value=/data/choice]")
            )
        ));

        int evaluations = Measure.withMeasure(asList("PredicateEvaluation", "IndexEvaluation"), () -> {
            scenario.answer("/data/choice", "a");

            scenario.choicesOf("/data/select1");
            scenario.choicesOf("/data/select2");
        });

        // Check that we do just (size of secondary instance)
        assertThat(evaluations, equalTo(2));
    }

    @Test
    public void repeatedCompChoiceFiltersAreOnlyEvaluatedOnce_whileLiteralExpressionIsTheSame() throws Exception {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("choice"),
                        t("select1"),
                        t("select2")
                    )),
                    instance("instance",
                        item("1", "A"),
                        item("2", "B")
                    ),
                    bind("/data/choice").type("string"),
                    bind("/data/select1").type("string"),
                    bind("/data/select2").type("string")
                )
            ),
            body(
                input("/data/choice"),
                select1Dynamic("/data/select1", "instance('instance')/root/item[value</data/choice]"),
                select1Dynamic("/data/select2", "instance('instance')/root/item[value</data/choice]")
            )
        ));

        int evaluations = Measure.withMeasure(asList("PredicateEvaluation", "IndexEvaluation"), () -> {
            scenario.answer("/data/choice", "3");

            scenario.choicesOf("/data/select1");
            scenario.choicesOf("/data/select2");
        });

        // Check that we do just (size of secondary instance)
        assertThat(evaluations, equalTo(2));
    }

    @Test
    public void repeatedEqChoiceFiltersAreOnlyEvaluatedOnce() throws Exception {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("choice"),
                        t("select1"),
                        t("select2")
                    )),
                    instance("instance",
                        item("a", "A"),
                        item("b", "B")
                    ),
                    bind("/data/choice").type("string"),
                    bind("/data/select1").type("string"),
                    bind("/data/select2").type("string")
                )
            ),
            body(
                input("/data/choice"),
                select1Dynamic("/data/select1", "instance('instance')/root/item[value=/data/choice]"),
                select1Dynamic("/data/select2", "instance('instance')/root/item[value=/data/choice]")
            )
        ));

        int evaluations = Measure.withMeasure(asList("PredicateEvaluation", "IndexEvaluation"), () -> {
            scenario.answer("/data/choice", "a");
            scenario.choicesOf("/data/select1");

            scenario.answer("/data/choice", "b");
            scenario.choicesOf("/data/select2");
        });

        // Check that we do just (size of secondary instance)
        assertThat(evaluations, equalTo(2));
    }

    @Test
    public void nestedPredicatesAreOnlyEvaluatedOnceForAQuestionWhileTheFormStateIsStable() throws Exception {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("choice"),
                        t("other_choice"),
                        t("select")
                    )),
                    instance("instance",
                        item("a", "A"),
                        item("b", "B")
                    ),
                    bind("/data/choice").type("string"),
                    bind("/data/other_choice").type("string"),
                    bind("/data/select").type("string")
                )
            ),
            body(
                input("/data/choice"),
                input("/data/other_choice"),
                select1Dynamic("/data/select", "instance('instance')/root/item[value=/data/choice][value=/data/other_choice]")
            )
        ));

        int evaluations = Measure.withMeasure(asList("PredicateEvaluation", "IndexEvaluation"), () -> {
            scenario.answer("/data/choice", "a");
            scenario.answer("/data/other_choice", "a");

            scenario.choicesOf("/data/select");
            scenario.choicesOf("/data/select");
        });

        /* Check that we do less than (secondary instance size) * (number of lookups) - the number could fluctuate
          depending on how the nested predicates are individually cached/indexed.
         */
        assertThat(evaluations, lessThan(4));
    }

    @Test
    public void nestedPredicatesAreCorrectAfterFormStateChanges() throws Exception {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("choice"),
                        t("other_choice"),
                        t("select")
                    )),
                    instance("instance",
                        item("a", "A"),
                        item("b", "B")
                    ),
                    bind("/data/choice").type("string"),
                    bind("/data/other_choice").type("string"),
                    bind("/data/select").type("string")
                )
            ),
            body(
                input("/data/choice"),
                input("/data/other_choice"),
                select1Dynamic("/data/select", "instance('instance')/root/item[value=/data/choice][value=/data/other_choice]")
            )
        ));

        scenario.answer("/data/choice", "a");
        scenario.answer("/data/other_choice", "a");
        assertThat(scenario.choicesOf("/data/select").size(), equalTo(1));

        scenario.answer("/data/other_choice", "b");
        assertThat(scenario.choicesOf("/data/select").size(), equalTo(0));
    }

    //region repeats
    @Test
    public void eqChoiceFilter_inRepeat_onlyEvaluatedOnce() throws Exception {
        Scenario scenario = Scenario.init("Select in repeat", html(
            head(
                title("Select in repeat"),
                model(
                    mainInstance(
                        t("data id='repeat-select'",
                            t("filter"),
                            t("repeat",
                                t("select")))),

                    instance("choices",
                        item("a", "A"),
                        item("aa", "AA"),
                        item("b", "B"),
                        item("bb", "BB")))),
            body(
                input("filter"),
                repeat("/data/repeat",
                    select1Dynamic("/data/repeat/select", "instance('choices')/root/item[value=/data/filter]"))
            )));

        int evaluations = Measure.withMeasure(asList("PredicateEvaluation", "IndexEvaluation"), () -> {
            scenario.answer("/data/filter", "a");

            scenario.choicesOf("/data/repeat[1]/select");

            scenario.createNewRepeat("/data/repeat");
            scenario.choicesOf("/data/repeat[2]/select");
        });

        // Check that we do just (size of secondary instance)
        assertThat(evaluations, equalTo(4));
    }

    @Test
    public void eqChoiceFiltersInRepeatsWithCurrentPathExpressionsAreOnlyEvaluatedOnce() throws Exception {
        Scenario scenario = Scenario.init("Select in repeat", html(
            head(
                title("Select in repeat"),
                model(
                    mainInstance(
                        t("data id='repeat-select'",
                            t("outer",
                                t("filter"),
                                t("inner",
                                    t("select"))))),

                    instance("choices",
                        item("a", "A"),
                        item("b", "B")))),
            body(
                repeat("/data/outer",
                    input("filter"),
                    repeat("/data/outer/inner",
                        select1Dynamic("/data/outer/inner/select", "instance('choices')/root/item[value=current()/../../filter]"))
                ))));

        scenario.answer("/data/outer[1]/filter", "a");
        scenario.createNewRepeat("/data/outer[1]/inner");
        scenario.answer("/data/outer[2]/filter", "a");
        scenario.createNewRepeat("/data/outer[2]/inner");
        scenario.createNewRepeat("/data/outer[2]/inner");

        int evaluations = Measure.withMeasure(asList("PredicateEvaluation", "IndexEvaluation"), () -> {
            scenario.choicesOf("/data/outer[1]/inner[1]/select");
            scenario.choicesOf("/data/outer[1]/inner[2]/select");
        });

        // Check that we do just (size of secondary instance)
        assertThat(evaluations, equalTo(2));
    }
    //endregion

    @Test
    public void eqChoiceFiltersForIntsWork() throws Exception {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("choice"),
                        t("select")
                    )),
                    instance("instance",
                        item("1", "One"),
                        item("2", "Two")
                    ),
                    bind("/data/choice").type("int"),
                    bind("/data/select").type("string")
                )
            ),
            body(
                input("/data/choice"),
                select1Dynamic("/data/select", "instance('instance')/root/item[value=/data/choice]")
            )
        ));

        scenario.answer("/data/choice", 1);
        assertThat(scenario.choicesOf("/data/select").size(), equalTo(1));
    }
}
