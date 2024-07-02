/*
 * Copyright 2018 Nafundi
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
package org.javarosa.xpath.expr;

import static org.hamcrest.Matchers.is;
import static org.javarosa.core.test.AnswerDataMatchers.stringAnswer;
import static org.javarosa.test.BindBuilderXFormsElement.bind;
import static org.javarosa.test.XFormsElement.body;
import static org.javarosa.test.XFormsElement.head;
import static org.javarosa.test.XFormsElement.html;
import static org.javarosa.test.XFormsElement.input;
import static org.javarosa.test.XFormsElement.mainInstance;
import static org.javarosa.test.XFormsElement.model;
import static org.javarosa.test.XFormsElement.t;
import static org.javarosa.test.XFormsElement.title;
import static org.javarosa.xpath.expr.DigestAlgorithm.MD5;
import static org.javarosa.xpath.expr.DigestAlgorithm.SHA1;
import static org.javarosa.xpath.expr.DigestAlgorithm.SHA256;
import static org.javarosa.xpath.expr.DigestAlgorithm.SHA384;
import static org.javarosa.xpath.expr.DigestAlgorithm.SHA512;
import static org.javarosa.xpath.expr.Encoding.BASE64;
import static org.javarosa.xpath.expr.Encoding.HEX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Arrays;
import org.javarosa.test.Scenario;
import org.javarosa.xform.parse.XFormParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class DigestTest {
    @Parameterized.Parameter()
    public String testName;

    @Parameterized.Parameter(value = 1)
    public DigestAlgorithm algorithm;

    @Parameterized.Parameter(value = 2)
    public String input;

    @Parameterized.Parameter(value = 3)
    public Encoding encoding;

    @Parameterized.Parameter(value = 4)
    public String expectedOutput;

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {"MD5", MD5, "some text", HEX, "552e21cd4cd9918678e3c1a0df491bc3"},
            {"SHA-1", SHA1, "some text", HEX, "37aa63c77398d954473262e1a0057c1e632eda77"},
            {"SHA-256", SHA256, "some text", HEX, "b94f6f125c79e3a5ffaa826f584c10d52ada669e6762051b826b55776d05aed2"},
            {"SHA-384", SHA384, "some text", HEX, "cc94ec3e9873c0b9a72486442958f671067cdf77b9427416d031440cc62041e2ee1344498447ec0ced9f7043461bd1f3"},
            {"SHA-512", SHA512, "some text", HEX, "e2732baedca3eac1407828637de1dbca702c3fc9ece16cf536ddb8d6139cd85dfe7464b8235b29826f608ccf4ac643e29b19c637858a3d8710a59111df42ddb5"},

            {"MD5", MD5, "some text", BASE64, "VS4hzUzZkYZ448Gg30kbww=="},
            {"SHA-1", SHA1, "some text", BASE64, "N6pjx3OY2VRHMmLhoAV8HmMu2nc="},
            {"SHA-256", SHA256, "some text", BASE64, "uU9vElx546X/qoJvWEwQ1SraZp5nYgUbgmtVd20FrtI="},
            {"SHA-384", SHA384, "some text", BASE64, "zJTsPphzwLmnJIZEKVj2cQZ833e5QnQW0DFEDMYgQeLuE0RJhEfsDO2fcENGG9Hz"},
            {"SHA-512", SHA512, "some text", BASE64, "4nMrrtyj6sFAeChjfeHbynAsP8ns4Wz1Nt241hOc2F3+dGS4I1spgm9gjM9KxkPimxnGN4WKPYcQpZER30LdtQ=="},

            {"MD5 HEX of empty string (baseline from forum feedback)", MD5, "", HEX, "d41d8cd98f00b204e9800998ecf8427e"}
        });
    }

    @Test
    public void generates_a_digest() {
        assertEquals(expectedOutput, algorithm.digest(input, encoding));
    }

    @Test
    public void digestFunction_acceptsDynamicParameters() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Digest form"),
                model(
                    mainInstance(t("data id=\"digest\"",
                        t("my-text", input),
                        t("my-algorithm", algorithm.toString()),
                        t("my-encoding", encoding.toString()),
                        t("my-digest")
                    )),
                    bind("/data/my-text").type("string"),
                    bind("/data/my-algorithm").type("string"),
                    bind("/data/my-encoding").type("string"),
                    bind("/data/my-digest").type("string").calculate("digest(/data/my-text, /data/my-algorithm, /data/my-encoding)")
                )
            ),
            body(
                input("/data/my-text"),
                input("/data/my-algorithm"),
                input("/data/my-encoding")
            ))
        );

        assertThat(scenario.answerOf("/data/my-digest"), is(stringAnswer(expectedOutput)));
    }
}
