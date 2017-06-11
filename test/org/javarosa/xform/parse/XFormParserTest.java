package org.javarosa.xform.parse;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.RangeQuestion;
import org.junit.Test;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.javarosa.core.model.Constants.CONTROL_RANGE;
import static org.junit.Assert.assertEquals;

public class XFormParserTest {
    @Test
    public void parsesSimpleForm() throws IOException {
        FormDef formDef = parse("simple-form.xml").formDef;
        assertEquals(formDef.getTitle(), "Simple Form");
    }

    @Test
    public void parsesForm2() throws IOException {
        FormDef formDef = parse("form2.xml").formDef;
        assertEquals("My Survey", formDef.getTitle());
        assertEquals(3, formDef.getChildren().size());
        assertEquals("What is your first name?", formDef.getChild(0).getLabelInnerText());
    }

    @Test
    public void parsesRangeForm() throws IOException {
        FormDef formDef = parse("range-form.xml").formDef;
        RangeQuestion question = (RangeQuestion) formDef.getChild(0);
        assertEquals(CONTROL_RANGE, question.getControlType());
        assertEquals(-2.0d, question.getRangeStart().doubleValue(), 0);
        assertEquals( 2.0d, question.getRangeEnd()  .doubleValue(), 0);
        assertEquals( 0.5d, question.getRangeStep() .doubleValue(), 0);
    }

    @Test(expected = XFormParseException.class)
    public void throwsParseExceptionOnBadRangeForm() throws IOException {
        parse("bad-range-form.xml");
    }

    @Test
    public void parsesMetaNamespaceForm() throws IOException {
        ParseResult parseResult = parse("meta-namespace-form.xml");
        assertEquals(parseResult.formDef.getTitle(), "Namespace for Metadata");
        assertEquals("Number of error messages", 0, parseResult.errorMessages.size());
    }

    private ParseResult parse(String formName) throws IOException {
        XFormParser parser = new XFormParser(new FileReader("resources/" + formName));
        final List<String> errorMessages = new ArrayList<>();
        parser.reporter = new XFormParserReporter() {
            @Override
            public void warning(String type, String message, String xmlLocation) {
                errorMessages.add(message);
                super.warning(type, message, xmlLocation);
            }

            @Override
            public void error(String message) {
                errorMessages.add(message);
                super.error(message);
            }
        };
        return new ParseResult(parser.parse(), errorMessages);
    }

    class ParseResult {
        final FormDef formDef;
        final List<String> errorMessages;

        ParseResult(FormDef formDef, List<String> errorMessages) {
            this.formDef = formDef;
            this.errorMessages = errorMessages;
        }
    }
}
