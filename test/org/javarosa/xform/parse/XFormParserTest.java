package org.javarosa.xform.parse;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.QuestionDef;
import org.junit.Test;

import java.io.FileReader;
import java.io.IOException;

import static org.javarosa.core.model.Constants.CONTROL_RANGE;
import static org.junit.Assert.assertEquals;


public class XFormParserTest {
    @Test public void parsesSimpleForm() throws IOException {
        FormDef formDef = parse("simple-form.xml");
        assertEquals(formDef.getTitle(), "Simple Form");
    }

    @Test public void parsesForm2() throws IOException {
        FormDef formDef = parse("form2.xml");
        assertEquals("My Survey", formDef.getTitle());
        assertEquals(3, formDef.getChildren().size());
        assertEquals("What is your first name?", formDef.getChild(0).getLabelInnerText());
    }

    @Test public void parsesRangeForm() throws IOException {
        FormDef formDef = parse("range-form.xml");
        assertEquals(CONTROL_RANGE, ((QuestionDef) formDef.getChild(0)).getControlType());
    }

    private FormDef parse(String formName) throws IOException {
        XFormParser parser = new XFormParser(new FileReader("resources/" + formName));
        return parser.parse();
    }
}
