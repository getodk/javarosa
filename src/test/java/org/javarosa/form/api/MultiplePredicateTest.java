package org.javarosa.form.api;

import org.javarosa.core.test.Scenario;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.javarosa.core.util.BindBuilderXFormsElement.bind;
import static org.javarosa.core.util.XFormsElement.body;
import static org.javarosa.core.util.XFormsElement.head;
import static org.javarosa.core.util.XFormsElement.html;
import static org.javarosa.core.util.XFormsElement.input;
import static org.javarosa.core.util.XFormsElement.instance;
import static org.javarosa.core.util.XFormsElement.mainInstance;
import static org.javarosa.core.util.XFormsElement.model;
import static org.javarosa.core.util.XFormsElement.t;
import static org.javarosa.core.util.XFormsElement.title;

public class MultiplePredicateTest {

    @Test
    public void calculatesSupportMultiplePredicatesInOnePartOfPath() throws Exception {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("calc"),
                        t("input")
                    )),
                    instance("instance",
                        t("item",
                            t("value", "A"),
                            t("count", "2"),
                            t("id", "A2")
                        ),
                        t("item",
                            t("value", "A"),
                            t("count", "3"),
                            t("id", "A3")
                        ),
                        t("item",
                            t("value", "B"),
                            t("count", "2"),
                            t("id", "B2")
                        )
                    ),
                    bind("/data/calc").type("string")
                        .calculate("instance('instance')/root/item[value = 'A'][count = /data/input]/id"),
                    bind("/data/input").type("string")
                )
            ),
            body(input("/data/input"))
        ));

        scenario.answer("/data/input", "3");
        assertThat(scenario.answerOf("/data/calc").getValue(), equalTo("A3"));

        scenario.answer("/data/input", "2");
        assertThat(scenario.answerOf("/data/calc").getValue(), equalTo("A2"));

        scenario.answer("/data/input", "7");
        assertThat(scenario.answerOf("/data/calc"), nullValue());
    }

    @Test
    public void calculatesSupportMultiplePredicatesInMultiplePartsOfPath() throws Exception {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("calc"),
                        t("input")
                    )),
                    instance("instance",
                        t("item",
                            t("name", "Bob Smith"),
                            t("yob", "1966"),
                            t("child",
                                t("name", "Sally Smith"),
                                t("yob", "1988")
                            ),
                            t("child",
                                t("name", "Kwame Smith"),
                                t("yob", "1990"))
                        ),
                        t("item",
                            t("name", "Hu Xao"),
                            t("yob", "1972"),
                            t("child",
                                t("name", "Foo Bar"),
                                t("yob", "1988")
                            ),
                            t("child",
                                t("name", "Foo2 Bar"),
                                t("yob", "2008")
                            )
                        ),
                        t("item",
                            t("name", "Baz Quux"),
                            t("yob", "1968"),
                            t("child",
                                t("name", "Baz2 Quux"),
                                t("yob", "1988")
                            ),
                            t("child",
                                t("name", "Baz3 Quux"),
                                t("yob", "1988")
                            )
                        )
                    ),
                    bind("/data/calc").type("string")
                        .calculate("count(instance('instance')/root/item[yob < 1970]/child[yob = 1988])"),
                    bind("/data/input").type("string")
                )
            ),
            body(input("/data/input"))
        ));

        assertThat(scenario.answerOf("/data/calc").getValue(), equalTo(3));
    }
}
