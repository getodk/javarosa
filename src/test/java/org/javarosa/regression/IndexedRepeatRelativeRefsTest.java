/*
 * Copyright 2020 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.javarosa.regression;

import static org.hamcrest.Matchers.is;
import static org.javarosa.core.test.AnswerDataMatchers.intAnswer;
import static org.javarosa.core.util.BindBuilderXFormsElement.bind;
import static org.javarosa.core.util.XFormsElement.body;
import static org.javarosa.core.util.XFormsElement.group;
import static org.javarosa.core.util.XFormsElement.head;
import static org.javarosa.core.util.XFormsElement.html;
import static org.javarosa.core.util.XFormsElement.input;
import static org.javarosa.core.util.XFormsElement.mainInstance;
import static org.javarosa.core.util.XFormsElement.model;
import static org.javarosa.core.util.XFormsElement.repeat;
import static org.javarosa.core.util.XFormsElement.t;
import static org.javarosa.core.util.XFormsElement.title;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Arrays;
import org.javarosa.core.test.Scenario;
import org.javarosa.xform.parse.XFormParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class IndexedRepeatRelativeRefsTest {
    public static final String ABSOLUTE_TARGET = "/data/some-group/item/value";
    public static final String RELATIVE_TARGET = "../item/value";
    public static final String ABSOLUTE_GROUP = "/data/some-group/item";
    public static final String RELATIVE_GROUP = "../item";
    public static final String ABSOLUTE_INDEX = "/data/total-items";
    public static final String RELATIVE_INDEX = "../../total-items";

    @Parameterized.Parameter(value = 0)
    public String testName;

    @Parameterized.Parameter(value = 1)
    public String target;

    @Parameterized.Parameter(value = 2)
    public String group;

    @Parameterized.Parameter(value = 3)
    public String index;

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {"Target: absolute, group: absolute, index: absolute", ABSOLUTE_TARGET, ABSOLUTE_GROUP, ABSOLUTE_INDEX},
            {"Target: absolute, group: absolute, index: relative", ABSOLUTE_TARGET, ABSOLUTE_GROUP, RELATIVE_INDEX},
            {"Target: absolute, group: relative, index: absolute", ABSOLUTE_TARGET, RELATIVE_GROUP, ABSOLUTE_INDEX},
            {"Target: absolute, group: relative, index: relative", ABSOLUTE_TARGET, RELATIVE_GROUP, RELATIVE_INDEX},
            {"Target: relative, group: absolute, index: absolute", RELATIVE_TARGET, ABSOLUTE_GROUP, ABSOLUTE_INDEX},
            {"Target: relative, group: absolute, index: relative", RELATIVE_TARGET, ABSOLUTE_GROUP, RELATIVE_INDEX},
            {"Target: relative, group: relative, index: absolute", RELATIVE_TARGET, RELATIVE_GROUP, ABSOLUTE_INDEX},
            {"Target: relative, group: relative, index: relative", RELATIVE_TARGET, RELATIVE_GROUP, RELATIVE_INDEX}
        });
    }

    @Test
    public void indexed_repeat() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("some-group",
                            t("item jr:template=\"\"",
                                t("value")),
                            t("last-value")
                        ),
                        t("total-items")
                    )),
                    bind(ABSOLUTE_TARGET).type("int"),
                    bind("/data/total-items").type("int").calculate("count(/data/some-group/item)"),
                    bind("/data/some-group/last-value").type("int").calculate("indexed-repeat(" + target + ", " + group + ", " + index + ")")
                )
            ),
            body(
                group("/data/some-group",
                    group("/data/some-group/item",
                        repeat("/data/some-group/item",
                            input("/data/some-group/item/value")
                        )
                    )
                )
            )
        ));

        scenario.answer("/data/some-group[0]/item[0]/value", 11);
        scenario.answer("/data/some-group[0]/item[1]/value", 22);
        scenario.answer("/data/some-group[0]/item[2]/value", 33);

        assertThat(scenario.answerOf("/data/total-items"), is(intAnswer(3)));
        assertThat(scenario.answerOf("/data/some-group/last-value"), is(intAnswer(33)));
    }
}
