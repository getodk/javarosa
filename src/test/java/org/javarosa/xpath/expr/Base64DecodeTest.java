package org.javarosa.xpath.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.javarosa.core.test.AnswerDataMatchers.stringAnswer;
import static org.javarosa.core.util.BindBuilderXFormsElement.bind;
import static org.javarosa.core.util.XFormsElement.body;
import static org.javarosa.core.util.XFormsElement.head;
import static org.javarosa.core.util.XFormsElement.html;
import static org.javarosa.core.util.XFormsElement.input;
import static org.javarosa.core.util.XFormsElement.mainInstance;
import static org.javarosa.core.util.XFormsElement.model;
import static org.javarosa.core.util.XFormsElement.t;
import static org.javarosa.core.util.XFormsElement.title;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Arrays;
import org.javarosa.core.test.Scenario;
import org.javarosa.xform.parse.XFormParser;
import org.javarosa.xpath.XPathUnhandledException;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Enclosed.class)
public class Base64DecodeTest {

    @RunWith(Parameterized.class)
    public static class ValidValuesTest {
        @Parameterized.Parameter(value = 0)
        public String testName;

        @Parameterized.Parameter(value = 1)
        public String source;

        @Parameterized.Parameter(value = 2)
        public String expectedOutput;

        @Parameterized.Parameters(name = "{0}")
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][]{
                {"ASCII string", "SGVsbG8=", "Hello"},
                {"Example from Saxonica", "RGFzc2Vs", "Dassel"},
                {"String with accented characters", "w6nDqMOx", "éèñ"},
                {"String with emoji", "8J+lsA==", "🥰"},
                {"UTF-16 encoded string", "AGEAYgBj", "\u0000a\u0000b\u0000c"}, // source string: "abc" in UTF-16
            });
        }

        @Test
        public void base64DecodeFunction_acceptsDynamicParameters() throws IOException, XFormParser.ParseException {
            Scenario scenario = Scenario.init(testName, html(
                head(
                    title(testName),
                    model(
                        mainInstance(t("data id=\"base64\"",
                            t("text", source),
                            t("decoded")
                        )),
                        bind("/data/text").type("string"),
                        bind("/data/decoded").type("string").calculate("base64-decode(/data/text)")
                    )
                ),
                body(
                    input("/data/text")
                ))
            );

            assertThat(scenario.answerOf("/data/decoded"), is(stringAnswer(expectedOutput)));
        }
    }

    public static class InvalidValuesTest {
        @Test
        public void base64DecodeFunction_throwsWhenNotExactlyOneArg() throws IOException, XFormParser.ParseException {
            try {
                Scenario scenario = Scenario.init("Invalid base64 string", html(
                    head(
                        title("Invalid base64 string"),
                        model(
                            mainInstance(t("data id=\"base64\"",
                                t("text", "a"),
                                t("decoded")
                            )),
                            bind("/data/text").type("string"),
                            bind("/data/decoded").type("string").calculate("base64-decode()")
                        )
                    ),
                    body(
                        input("/data/text")
                    ))
                );

                fail("RuntimeException caused by XPathUnhandledException expected");
            } catch (RuntimeException e) {
                assertThat(e.getCause(), instanceOf(XPathUnhandledException.class));
            }
        }

        @Test
        public void base64DecodeFunction_returnsEmptyStringWhenInputInvalid() throws IOException, XFormParser.ParseException {
            Scenario scenario = Scenario.init("Invalid base64 string", html(
                head(
                    title("Invalid base64 string"),
                    model(
                        mainInstance(t("data id=\"base64\"",
                            t("text", "a"),
                            t("decoded")
                        )),
                        bind("/data/text").type("string"),
                        bind("/data/decoded").type("string").calculate("base64-decode(/data/text)")
                    )
                ),
                body(
                    input("/data/text")
                ))
            );

            assertThat(scenario.answerOf("/data/decoded"), is(nullValue()));
        }
    }
}