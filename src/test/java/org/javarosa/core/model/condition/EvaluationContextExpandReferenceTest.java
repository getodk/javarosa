/*
 * Copyright 2019 Nafundi
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

package org.javarosa.core.model.condition;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.javarosa.core.model.instance.TestHelpers.buildRef;
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
import java.util.List;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.test.Scenario;
import org.junit.BeforeClass;
import org.junit.Test;

public class EvaluationContextExpandReferenceTest {
    private static Scenario scenario;
    private static EvaluationContext ec;

    @BeforeClass
    public static void setUp() throws IOException {
        scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("group jr:template=\"\"", t("number"))
                    )),
                    bind("/data/group/number").type("int")
                )
            ),
            body(group("/data/group", repeat("/data/group", input("/data/group/number"))))
        ));
        scenario.next();
        range(0, 5).forEach(__ -> {
            scenario.createNewRepeat();
            scenario.next();
            scenario.next();
        });
        ec = scenario.getEvaluationContext();
    }

    @Test
    public void test_normal_case() {
        // Using the String representation to simplify things, since TreeReference.equals
        // wouldn't work the way we want, and TestHelpers.buildRef() has its limitations
        assertThat(getRefStringList(ec.expandReference(buildRef("/data/group/number"))), hasItems(
            "/data/group[0]/number[0]",
            "/data/group[1]/number[0]",
            "/data/group[2]/number[0]",
            "/data/group[3]/number[0]",
            "/data/group[4]/number[0]"
        ));
    }

    @Test
    public void test_include_templates_case() {
        assertThat(getRefStringList(ec.expandReference(buildRef("/data/group/number"), true)), hasItems(
            "/data/group[@template]/number[0]",
            "/data/group[0]/number[0]",
            "/data/group[1]/number[0]",
            "/data/group[2]/number[0]",
            "/data/group[3]/number[0]",
            "/data/group[4]/number[0]"
        ));
    }

    @Test
    public void test_relative_ref_case() {
        assertThat(ec.expandReference(buildRef("group/number")), is(nullValue()));
    }

    private static List<String> getRefStringList(List<TreeReference> treeReferences) {
        // Using the String representation to simplify things, since TreeReference.equals
        // wouldn't work the way we want, and TestHelpers.buildRef() has its limitations
        return treeReferences.stream().map(ref -> ref.toString(true, true)).collect(toList());
    }
}
