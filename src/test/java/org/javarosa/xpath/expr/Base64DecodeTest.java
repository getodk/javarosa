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
import org.javarosa.core.test.Scenario;
import org.javarosa.xform.parse.ParseException;
import org.javarosa.xpath.XPathUnhandledException;
import org.junit.Test;

public class Base64DecodeTest {

    @Test
    public void asciiString_isSuccessfullyDecoded() throws IOException, ParseException {
        Scenario scenario = getBase64DecodeScenario("ASCII string", "SGVsbG8=");
        assertThat(scenario.answerOf("/data/decoded"), is(stringAnswer("Hello")));
    }

    @Test
    public void exampleFromSaxonica_isSuccessfullyDecoded() throws IOException, ParseException {
        Scenario scenario = getBase64DecodeScenario("Example from Saxonica", "RGFzc2Vs");
        assertThat(scenario.answerOf("/data/decoded"), is(stringAnswer("Dassel")));
    }

    @Test
    public void accentString_isSuccessfullyDecoded() throws IOException, ParseException {
        Scenario scenario = getBase64DecodeScenario("String with accented characters", "w6nDqMOx");
        assertThat(scenario.answerOf("/data/decoded"), is(stringAnswer("éèñ")));
    }

    @Test
    public void emojiString_isSuccessfullyDecoded() throws IOException, ParseException {
        Scenario scenario = getBase64DecodeScenario("String with emoji", "8J+lsA==");
        assertThat(scenario.answerOf("/data/decoded"), is(stringAnswer("🥰")));
    }

    @Test
    public void utf16String_isDecodedToGarbage() throws IOException, ParseException {
        Scenario scenario = getBase64DecodeScenario("UTF-16 encoded string", "AGEAYgBj");
        assertThat(scenario.answerOf("/data/decoded"), is(stringAnswer("\u0000a\u0000b\u0000c"))); // source string: "abc" in UTF-16
    }

    private static Scenario getBase64DecodeScenario(String testName, String source) throws IOException, ParseException {
        return Scenario.init(testName, html(
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
    }

    @Test
    public void base64DecodeFunction_throwsWhenNotExactlyOneArg() throws IOException, ParseException {
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
    public void base64DecodeFunction_returnsEmptyStringWhenInputInvalid() throws IOException, ParseException {
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