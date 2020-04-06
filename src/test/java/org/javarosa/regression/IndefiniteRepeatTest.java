package org.javarosa.regression;

import org.javarosa.core.test.Scenario;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.javarosa.core.util.BindBuilderXFormsElement.bind;
import static org.javarosa.core.util.XFormsElement.body;
import static org.javarosa.core.util.XFormsElement.head;
import static org.javarosa.core.util.XFormsElement.html;
import static org.javarosa.core.util.XFormsElement.input;
import static org.javarosa.core.util.XFormsElement.mainInstance;
import static org.javarosa.core.util.XFormsElement.model;
import static org.javarosa.core.util.XFormsElement.repeat;
import static org.javarosa.core.util.XFormsElement.t;
import static org.javarosa.core.util.XFormsElement.title;

public class IndefiniteRepeatTest {
    @Test
    public void indefiniteRepeatJrCountExpression_inSingleRepeat_addsRepeatsUntilConditionMet() throws IOException {
        Scenario scenario = Scenario.init("indefinite repeat", html(
            head(
                title("Indefinite repeat"),
                model(
                    mainInstance(t("data id=\"indefinite-repeat\"",
                        t("count"),
                        t("target_count"),
                        t("repeat",
                            t("add_more")
                        )
                    )),
                    bind("/data/count").type("int").calculate("count(/data/repeat)"),
                    bind("/data/target_count").type("int").calculate("if(/data/count = 0 or /data/repeat[position()=/data/count]/add_more = 'yes', /data/count + 1, /data/count)"),
                    bind("/data/repeat/add_more").type("string")
                )),
            body(
                repeat("/data/repeat", "/data/target_count",
                    input("/data/repeat/add_more")
                )
            )
        ));

        scenario.next();
        scenario.next();
        scenario.answer("yes");
        scenario.next();
        scenario.next();
        scenario.answer("yes");
        scenario.next();
        scenario.next();
        scenario.answer("no");
        scenario.next();
        assertThat(scenario.atTheEndOfForm(), is(true));
    }

    @Test
    public void indefiniteRepeatJrCountExpression_inNestedRepeat_addsRepeatsUntilConditionMet() throws IOException {
        Scenario scenario = Scenario.init("nested indefinite repeat", html(
            head(
                title("Indefinite repeat in nested repeat"),
                model(
                    mainInstance(t("data id=\"indefinite-nested-repeat\"",
                        t("outer_repeat",
                            t("inner_count"),
                            t("target_count"),
                            t("inner_repeat",
                                t("add_more")
                            )
                        ))
                    ),
                    bind("/data/outer_repeat/inner_count").type("int").calculate("count(/data/outer_repeat/inner_repeat)"),
                    bind("/data/outer_repeat/target_count").type("int").calculate("if(/data/outer_repeat/inner_count = 0" +
                        "or /data/outer_repeat/inner_repeat[position() = /data/outer_repeat/inner_count]/add_more = 'yes', " +
                        "/data/outer_repeat/inner_count + 1, /data/outer_repeat/inner_count)")
                )),
            body(
                repeat("/data/outer_repeat",
                    repeat("/data/outer_repeat/inner_repeat", "target_count",
                        input("/data/outer_repeat/inner_repeat/add_more")
                    )
                )
            )));

        scenario.next();
        scenario.next();
        scenario.next();
        scenario.answer("yes");
        scenario.next();
        scenario.next();
        scenario.answer("yes");
        scenario.next();
        scenario.next();
        scenario.answer("no");
        scenario.next();
        scenario.createNewRepeat();
        scenario.next();
        scenario.next();
        scenario.answer("yes");
        scenario.next();
        scenario.next();
        scenario.answer("no");
        scenario.next();
        scenario.next();
        assertThat(scenario.atTheEndOfForm(), is(true));
    }
}
