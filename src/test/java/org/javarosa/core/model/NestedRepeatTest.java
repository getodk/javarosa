package org.javarosa.core.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.javarosa.core.test.AnswerDataMatchers.stringAnswer;
import static org.javarosa.test.BindBuilderXFormsElement.bind;
import static org.javarosa.test.XFormsElement.body;
import static org.javarosa.test.XFormsElement.head;
import static org.javarosa.test.XFormsElement.html;
import static org.javarosa.test.XFormsElement.input;
import static org.javarosa.test.XFormsElement.mainInstance;
import static org.javarosa.test.XFormsElement.model;
import static org.javarosa.test.XFormsElement.repeat;
import static org.javarosa.test.XFormsElement.t;
import static org.javarosa.test.XFormsElement.title;

import org.javarosa.test.Scenario;
import org.junit.Test;

public class NestedRepeatTest {

    @Test
    public void nestedRepeatWithConcreteInstance_canBeReachedAndAnswered() throws Exception {
        Scenario scenario = Scenario.init(html(
            head(
                title("Nested repeat instances"),
                model(
                    mainInstance(t("data id=\"nested-repeat-instances\"",
                        t("outer jr:template=\"\"",
                            t("outerValue"),
                            t("inner jr:template=\"\"", t("value")),
                            t("inner", t("value"))),
                        t("outer", t("outerValue"), t("inner", t("value")))
                    ))
                )
            ),
            body(
                repeat("/data/outer",
                    input("/data/outer/outerValue"),
                    repeat("/data/outer/inner",
                        input("/data/outer/inner/value")
                    )
                )
            )
        ));

        assertThat(scenario.countRepeatInstancesOf("/data/outer"), is(1));
        assertThat(scenario.countRepeatInstancesOf("/data/outer[1]/inner"), is(1));
        scenario.next(4);
        scenario.answer("foo");

        assertThat(scenario.answerOf("/data/outer[1]/inner[1]/value"), is(stringAnswer("foo")));
    }

    @Test
    // pyxform output before https://github.com/XLSForm/pyxform/pull/876
    public void creatingOuterRepeat_withoutInnerConcreteInstance_createsNoInnerInstance() throws Exception {
        Scenario scenario = Scenario.init(html(
            head(
                title("Nested repeat with prior pyxform structure"),
                model(
                    mainInstance(t("data id=\"nested-repeat-prior-pyxform\"",
                        t("outer jr:template=\"\"",
                            t("outerValue"),
                            t("inner jr:template=\"\"", t("innerValue"))
                        ),
                        t("outer",
                            t("outerValue"),
                            t("inner", t("innerValue"))
                        )
                    ))
                )
            ),
            body(
                repeat("/data/outer",
                    input("/data/outer/outerValue"),
                    repeat("/data/outer/inner",
                        input("/data/outer/inner/innerValue")
                    )
                )
            )
        ));

        assertThat(scenario.countRepeatInstancesOf("/data/outer"), is(1));
        assertThat(scenario.countRepeatInstancesOf("/data/outer[1]/inner"), is(1));

        scenario.createNewRepeat("/data/outer");

        assertThat(scenario.countRepeatInstancesOf("/data/outer"), is(2));
        assertThat(scenario.countRepeatInstancesOf("/data/outer[1]/inner"), is(1));
        assertThat(scenario.countRepeatInstancesOf("/data/outer[2]/inner"), is(0));
    }

    @Test
    public void creatingOuterRepeat_withConcreteInnerInstance_createsOneInnerInstance() throws Exception {
        Scenario scenario = Scenario.init(html(
            head(
                title("New outer repeat with concrete inner starter"),
                model(
                    mainInstance(t("data id=\"nested-repeat-new-outer-with-inner\"",
                        t("outer jr:template=\"\"",
                            t("outerValue"),
                            t("inner jr:template=\"\"", t("value")),
                            t("inner", t("value"))),
                        t("outer",
                            t("outerValue"),
                            t("inner", t("value")))
                    ))
                )
            ),
            body(
                repeat("/data/outer",
                    input("/data/outer/outerValue"),
                    repeat("/data/outer/inner",
                        input("/data/outer/inner/value")
                    )
                )
            )
        ));

        assertThat(scenario.countRepeatInstancesOf("/data/outer"), is(1));
        assertThat(scenario.countRepeatInstancesOf("/data/outer[1]/inner"), is(1));

        scenario.createNewRepeat("/data/outer");

        assertThat(scenario.countRepeatInstancesOf("/data/outer"), is(2));
        assertThat(scenario.countRepeatInstancesOf("/data/outer[1]/inner"), is(1));
        assertThat(scenario.countRepeatInstancesOf("/data/outer[2]/inner"), is(1));

        scenario.answer("/data/outer[2]/inner[1]/value", "second outer");

        assertThat(scenario.answerOf("/data/outer[2]/inner[1]/value"), is(stringAnswer("second outer")));
    }

    @Test
    public void creatingInnerRepeat_inSecondOuterInstance_doesNotAffectFirstOuterInstance() throws Exception {
        Scenario scenario = Scenario.init(html(
            head(
                title("Inner repeats belong to their outer instance"),
                model(
                    mainInstance(t("data id=\"nested-repeat-independent-outer\"",
                        t("outer jr:template=\"\"",
                            t("outerValue"),
                            t("inner jr:template=\"\"", t("value")),
                            t("inner", t("value"))),
                        t("outer",
                            t("outerValue"),
                            t("inner", t("value")))
                    ))
                )
            ),
            body(
                repeat("/data/outer",
                    input("/data/outer/outerValue"),
                    repeat("/data/outer/inner",
                        input("/data/outer/inner/value")
                    )
                )
            )
        ));

        scenario.createNewRepeat("/data/outer");

        assertThat(scenario.countRepeatInstancesOf("/data/outer"), is(2));
        assertThat(scenario.countRepeatInstancesOf("/data/outer[1]/inner"), is(1));
        assertThat(scenario.countRepeatInstancesOf("/data/outer[2]/inner"), is(1));

        scenario.answer("/data/outer[1]/inner[1]/value", "first outer");
        scenario.answer("/data/outer[2]/inner[1]/value", "second outer");

        scenario.createNewRepeat("/data/outer[2]/inner");

        assertThat(scenario.countRepeatInstancesOf("/data/outer[1]/inner"), is(1));
        assertThat(scenario.countRepeatInstancesOf("/data/outer[2]/inner"), is(2));

        scenario.answer("/data/outer[2]/inner[2]/value", "new inner");

        assertThat(scenario.answerOf("/data/outer[1]/inner[1]/value"), is(stringAnswer("first outer")));
        assertThat(scenario.answerOf("/data/outer[2]/inner[1]/value"), is(stringAnswer("second outer")));
        assertThat(scenario.answerOf("/data/outer[2]/inner[2]/value"), is(stringAnswer("new inner")));
    }

    @Test
    public void creatingOuterRepeat_withThreeLevelNestedStarters_createsConcreteDescendants() throws Exception {
        Scenario scenario = Scenario.init(html(
            head(
                title("Three-level nested repeat starter instances"),
                model(
                    mainInstance(t("data id=\"three-level-nested-repeats\"",
                        t("outer jr:template=\"\"",
                            t("outerValue"),
                            t("middle jr:template=\"\"",
                                t("middleValue"),
                                t("inner jr:template=\"\"", t("value")),
                                t("inner", t("value"))
                            ),
                            t("middle",
                                t("middleValue"),
                                t("inner", t("value"))
                            )
                        ),
                        t("outer",
                            t("outerValue"),
                            t("middle",
                                t("middleValue"),
                                t("inner", t("value"))
                            )
                        )
                    ))
                )
            ),
            body(
                repeat("/data/outer",
                    input("/data/outer/outerValue"),
                    repeat("/data/outer/middle",
                        input("/data/outer/middle/middleValue"),
                        repeat("/data/outer/middle/inner",
                            input("/data/outer/middle/inner/value")
                        )
                    )
                )
            )
        ));

        assertThat(scenario.countRepeatInstancesOf("/data/outer"), is(1));
        assertThat(scenario.countRepeatInstancesOf("/data/outer[1]/middle"), is(1));
        assertThat(scenario.countRepeatInstancesOf("/data/outer[1]/middle[1]/inner"), is(1));

        scenario.createNewRepeat("/data/outer");

        assertThat(scenario.countRepeatInstancesOf("/data/outer"), is(2));
        assertThat(scenario.countRepeatInstancesOf("/data/outer[1]/middle"), is(1));
        assertThat(scenario.countRepeatInstancesOf("/data/outer[1]/middle[1]/inner"), is(1));
        assertThat(scenario.countRepeatInstancesOf("/data/outer[2]/middle"), is(1));
        assertThat(scenario.countRepeatInstancesOf("/data/outer[2]/middle[1]/inner"), is(1));
    }

    @Test
    public void creatingOuterRepeat_withInterveningGroup_createsConcreteInnerInstance() throws Exception {
        Scenario scenario = Scenario.init(html(
            head(
                title("Nested repeats separated by a group"),
                model(
                    mainInstance(t("data id=\"nested-repeat-with-group\"",
                        t("outer jr:template=\"\"",
                            t("outerValue"),
                            t("group",
                                t("inner jr:template=\"\"", t("value")),
                                t("inner", t("value"))
                            )
                        ),
                        t("outer",
                            t("outerValue"),
                            t("group",
                                t("inner", t("value"))
                            )
                        )
                    ))
                )
            ),
            body(
                repeat("/data/outer",
                    input("/data/outer/outerValue"),
                    repeat("/data/outer/group/inner",
                        input("/data/outer/group/inner/value")
                    )
                )
            )
        ));

        scenario.createNewRepeat("/data/outer");

        assertThat(scenario.countRepeatInstancesOf("/data/outer"), is(2));
        assertThat(scenario.countRepeatInstancesOf("/data/outer[1]/group/inner"), is(1));
        assertThat(scenario.countRepeatInstancesOf("/data/outer[2]/group/inner"), is(1));

        scenario.answer("/data/outer[2]/group[1]/inner[1]/value", "new outer");

        assertThat(scenario.answerOf("/data/outer[2]/group[1]/inner[1]/value"), is(stringAnswer("new outer")));
    }

    @Test
    public void creatingOuterRepeat_withThreeLevelStaticDefaults_preservesDefaultsInAllInstances() throws Exception {
        Scenario scenario = Scenario.init(html(
            head(
                title("Static defaults in three-level nested repeats"),
                model(
                    mainInstance(t("data id=\"three-level-static-defaults\"",
                        t("outer jr:template=\"\"",
                            t("outerValue", "outer default"),
                            t("middle jr:template=\"\"",
                                t("middleValue", "middle default"),
                                t("inner jr:template=\"\"",
                                    t("value", "inner default")
                                ),
                                t("inner",
                                    t("value", "inner default")
                                )
                            ),
                            t("middle",
                                t("middleValue", "middle default"),
                                t("inner",
                                    t("value", "inner default")
                                )
                            )
                        ),
                        t("outer",
                            t("outerValue", "outer default"),
                            t("middle",
                                t("middleValue", "middle default"),
                                t("inner",
                                    t("value", "inner default")
                                )
                            )
                        )
                    ))
                )
            ),
            body(
                repeat("/data/outer",
                    input("/data/outer/outerValue"),
                    repeat("/data/outer/middle",
                        input("/data/outer/middle/middleValue"),
                        repeat("/data/outer/middle/inner",
                            input("/data/outer/middle/inner/value")
                        )
                    )
                )
            )
        ));

        assertThat(scenario.answerOf("/data/outer[1]/outerValue"), is(stringAnswer("outer default")));
        assertThat(scenario.answerOf("/data/outer[1]/middle[1]/middleValue"), is(stringAnswer("middle default")));
        assertThat(scenario.answerOf("/data/outer[1]/middle[1]/inner[1]/value"), is(stringAnswer("inner default")));

        // Creating an outer repeat leads to defaults in its concrete descendants.
        scenario.createNewRepeat("/data/outer");
        assertThat(scenario.answerOf("/data/outer[2]/outerValue"), is(stringAnswer("outer default")));
        assertThat(scenario.answerOf("/data/outer[2]/middle[1]/middleValue"), is(stringAnswer("middle default")));
        assertThat(scenario.answerOf("/data/outer[2]/middle[1]/inner[1]/value"), is(stringAnswer("inner default")));

        // Creating a middle repeat uses its template with defaults for inner
        scenario.createNewRepeat("/data/outer[2]/middle");
        assertThat(scenario.countRepeatInstancesOf("/data/outer[2]/middle"), is(2));
        assertThat(scenario.countRepeatInstancesOf("/data/outer[2]/middle[2]/inner"), is(1));
        assertThat(scenario.answerOf("/data/outer[2]/middle[2]/middleValue"), is(stringAnswer("middle default")));
        assertThat(scenario.answerOf("/data/outer[2]/middle[2]/inner[1]/value"), is(stringAnswer("inner default")));

        // Creating an inner repeat uses its own template.
        scenario.createNewRepeat("/data/outer[2]/middle[2]/inner");
        assertThat(scenario.countRepeatInstancesOf("/data/outer[2]/middle[2]/inner"), is(2));
        assertThat(scenario.answerOf("/data/outer[2]/middle[2]/inner[1]/value"), is(stringAnswer("inner default")));
        assertThat(scenario.answerOf("/data/outer[2]/middle[2]/inner[2]/value"), is(stringAnswer("inner default")));
    }

    @Test
    public void creatingOuterRepeat_withThreeLevelDynamicDefaults_initializesConcreteDescendants() throws Exception {
        Scenario scenario = Scenario.init(html(
            head(
                title("Dynamic defaults in three-level nested repeats"),
                model(
                    mainInstance(t("data id=\"three-level-dynamic-defaults\"",
                        t("outer jr:template=\"\"",
                            t("outerValue"),
                            t("middle jr:template=\"\"",
                                t("middleValue"),
                                t("inner jr:template=\"\"",
                                    t("value")
                                ),
                                t("inner",
                                    t("value")
                                )
                            ),
                            t("middle",
                                t("middleValue"),
                                t("inner",
                                    t("value")
                                )
                            )
                        ),
                        t("outer",
                            t("outerValue"),
                            t("middle",
                                t("middleValue"),
                                t("inner",
                                    t("value")
                                )
                            )
                        )
                    )),
                    bind("/data/outer/outerValue").type("string"),
                    t("setvalue ref=\"/data/outer/outerValue\" event=\"odk-instance-first-load\" value=\"concat('outer', ' default')\""),
                    bind("/data/outer/middle/middleValue").type("string"),
                    t("setvalue ref=\"/data/outer/middle/middleValue\" event=\"odk-instance-first-load\" value=\"concat('middle', ' default')\""),
                    bind("/data/outer/middle/inner/value").type("string"),
                    t("setvalue ref=\"/data/outer/middle/inner/value\" event=\"odk-instance-first-load\" value=\"concat('inner', ' default')\"")
                )
            ),
            body(
                repeat("/data/outer",
                    input("/data/outer/outerValue"),
                    repeat("/data/outer/middle",
                        input("/data/outer/middle/middleValue"),
                        repeat("/data/outer/middle/inner",
                            input("/data/outer/middle/inner/value"),
                            t("setvalue ref=\"/data/outer/middle/inner/value\" event=\"odk-new-repeat\" value=\"concat('inner', ' default')\"")
                        ),
                        t("setvalue ref=\"/data/outer/middle/middleValue\" event=\"odk-new-repeat\" value=\"concat('middle', ' default')\"")
                    ),
                    t("setvalue ref=\"/data/outer/outerValue\" event=\"odk-new-repeat\" value=\"concat('outer', ' default')\"")
                )
            )
        ));

        assertThat(scenario.answerOf("/data/outer[1]/outerValue"), is(stringAnswer("outer default")));
        assertThat(scenario.answerOf("/data/outer[1]/middle[1]/middleValue"), is(stringAnswer("middle default")));
        assertThat(scenario.answerOf("/data/outer[1]/middle[1]/inner[1]/value"), is(stringAnswer("inner default")));

        scenario.createNewRepeat("/data/outer");

        assertThat(scenario.countRepeatInstancesOf("/data/outer[2]/middle"), is(1));
        assertThat(scenario.countRepeatInstancesOf("/data/outer[2]/middle[1]/inner"), is(1));

        assertThat(scenario.answerOf("/data/outer[2]/outerValue"), is(stringAnswer("outer default")));
        //assertThat(scenario.answerOf("/data/outer[2]/middle[1]/middleValue"), is(stringAnswer("middle default")));
        //assertThat(scenario.answerOf("/data/outer[2]/middle[1]/inner[1]/value"), is(stringAnswer("inner default")));
    }

    @Test
    public void creatingOuterRepeat_withDynamicDefaults_preservesExistingSiblingValues() throws Exception {
        Scenario scenario = Scenario.init(html(
            head(
                title("Dynamic defaults preserve existing sibling values"),
                model(
                    mainInstance(t("data id=\"nested-dynamic-defaults\"",
                        t("outer jr:template=\"\"",
                            t("outerValue"),
                            t("middle jr:template=\"\"",
                                t("middleValue"),
                                t("inner jr:template=\"\"",
                                    t("value")
                                ),
                                t("inner",
                                    t("value")
                                )
                            ),
                            t("middle",
                                t("middleValue"),
                                t("inner",
                                    t("value")
                                )
                            )
                        ),
                        t("outer",
                            t("outerValue"),
                            t("middle",
                                t("middleValue"),
                                t("inner",
                                    t("value")
                                )
                            )
                        )
                    )),
                    bind("/data/outer/outerValue").type("string"),
                    t("setvalue ref=\"/data/outer/outerValue\" event=\"odk-instance-first-load\" value=\"'outer default'\""),
                    bind("/data/outer/middle/middleValue").type("string"),
                    t("setvalue ref=\"/data/outer/middle/middleValue\" event=\"odk-instance-first-load\" value=\"'middle default'\""),
                    bind("/data/outer/middle/inner/value").type("string"),
                    t("setvalue ref=\"/data/outer/middle/inner/value\" event=\"odk-instance-first-load\" value=\"'inner default'\"")
                )
            ),
            body(
                repeat("/data/outer",
                    input("/data/outer/outerValue"),
                    repeat("/data/outer/middle",
                        input("/data/outer/middle/middleValue"),
                        repeat("/data/outer/middle/inner",
                            input("/data/outer/middle/inner/value"),
                            t("setvalue ref=\"/data/outer/middle/inner/value\" event=\"odk-new-repeat\" value=\"'inner default'\"")
                        ),
                        t("setvalue ref=\"/data/outer/middle/middleValue\" event=\"odk-new-repeat\" value=\"'middle default'\"")
                    ),
                    t("setvalue ref=\"/data/outer/outerValue\" event=\"odk-new-repeat\" value=\"'outer default'\"")
                )
            )
        ));

        scenario.next(2);
        scenario.answer("manual outer");

        scenario.next(2);
        scenario.answer("manual middle");

        scenario.next(2);
        scenario.answer("manual inner");

        scenario.createNewRepeat("/data/outer");

        assertThat(scenario.answerOf("/data/outer[1]/outerValue"), is(stringAnswer("manual outer")));
        assertThat(scenario.answerOf("/data/outer[1]/middle[1]/middleValue"), is(stringAnswer("manual middle")));
        assertThat(scenario.answerOf("/data/outer[1]/middle[1]/inner[1]/value"), is(stringAnswer("manual inner")));

        assertThat(scenario.answerOf("/data/outer[2]/outerValue"), is(stringAnswer("outer default")));
        //assertThat(scenario.answerOf("/data/outer[2]/middle[1]/middleValue"), is(stringAnswer("middle default")));
        //assertThat(scenario.answerOf("/data/outer[2]/middle[1]/inner[1]/value"), is(stringAnswer("inner default")));
    }

    @Test
    public void creatingNestedRepeats_withParentDependentDynamicDefaults_usesCorrectContext() throws Exception {
        Scenario scenario = Scenario.init(html(
            head(
                title("Parent-dependent dynamic defaults in nested repeats"),
                model(
                    mainInstance(t("data id=\"parent-dependent-defaults\"",
                        t("outer jr:template=\"\"",
                            t("outerValue"),
                            t("middle jr:template=\"\"",
                                t("middleValue"),
                                t("inner jr:template=\"\"",
                                    t("value")
                                ),
                                t("inner",
                                    t("value")
                                )
                            ),
                            t("middle",
                                t("middleValue"),
                                t("inner",
                                    t("value")
                                )
                            )
                        ),
                        t("outer",
                            t("outerValue"),
                            t("middle",
                                t("middleValue"),
                                t("inner",
                                    t("value")
                                )
                            )
                        )
                    )),
                    bind("/data/outer/outerValue").type("string"),
                    t("setvalue ref=\"/data/outer/outerValue\" event=\"odk-instance-first-load\" value=\"'outer default'\""),
                    bind("/data/outer/middle/middleValue").type("string"),
                    t("setvalue ref=\"/data/outer/middle/middleValue\" event=\"odk-instance-first-load\" value=\"concat(../../outerValue, ' middle')\""),
                    bind("/data/outer/middle/inner/value").type("string"),
                    t("setvalue ref=\"/data/outer/middle/inner/value\" event=\"odk-instance-first-load\" value=\"concat(../../middleValue, ' inner')\"")
                )
            ),
            body(
                repeat("/data/outer",
                    input("/data/outer/outerValue"),
                    repeat("/data/outer/middle",
                        input("/data/outer/middle/middleValue"),
                        repeat("/data/outer/middle/inner",
                            input("/data/outer/middle/inner/value"),
                            t("setvalue ref=\"/data/outer/middle/inner/value\" event=\"odk-new-repeat\" value=\"concat(../../middleValue, ' inner')\"")
                        ),
                        t("setvalue ref=\"/data/outer/middle/middleValue\" event=\"odk-new-repeat\" value=\"concat(../../outerValue, ' middle')\"")
                    ),
                    t("setvalue ref=\"/data/outer/outerValue\" event=\"odk-new-repeat\" value=\"'outer default'\"")
                )
            )
        ));

        assertThat(scenario.answerOf("/data/outer[1]/outerValue"), is(stringAnswer("outer default")));
        assertThat(scenario.answerOf("/data/outer[1]/middle[1]/middleValue"), is(stringAnswer("outer default middle")));
        assertThat(scenario.answerOf("/data/outer[1]/middle[1]/inner[1]/value"), is(stringAnswer("outer default middle inner")));

        scenario.createNewRepeat("/data/outer");

        assertThat(scenario.answerOf("/data/outer[2]/outerValue"), is(stringAnswer("outer default")));
        //assertThat(scenario.answerOf("/data/outer[2]/middle[1]/middleValue"), is(stringAnswer("outer default middle")));
        //assertThat(scenario.answerOf("/data/outer[2]/middle[1]/inner[1]/value"), is(stringAnswer("outer default middle inner")));

        scenario.createNewRepeat("/data/outer[2]/middle");

        assertThat(scenario.answerOf("/data/outer[2]/middle[2]/middleValue"), is(stringAnswer("outer default middle")));
        //assertThat(scenario.answerOf("/data/outer[2]/middle[2]/inner[1]/value"), is(stringAnswer("outer default middle inner")));

        scenario.createNewRepeat("/data/outer[2]/middle[2]/inner");

        assertThat(scenario.answerOf("/data/outer[2]/middle[2]/inner[2]/value"), is(stringAnswer("outer default middle inner")));

        scenario.createNewRepeat("/data/outer[2]/middle[2]/inner");

        assertThat(
            scenario.answerOf("/data/outer[2]/middle[2]/inner[2]/value"),
            is(stringAnswer("outer default middle inner"))
        );
    }
}