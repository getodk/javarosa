package org.javarosa.xform.parse;

import org.javarosa.core.model.FormDef;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.io.*;

public class XFormParserTest {
    @Test public void parsesSimpleForm() throws IOException {
        FormDef formDef = parse("simple-form.xml");
        assertEquals(formDef.getTitle(), "Simple Form");
    }

    @Test public void parsesForm2() throws IOException {
        FormDef formDef = parse("form2.xml");
        assertEquals(formDef.getTitle(), "My Survey");
        assertEquals(formDef.getChildren().size(), 3);
        assertEquals(formDef.getChild(0).getLabelInnerText(), "What is your first name?");
    }

    private FormDef parse(String formName) throws IOException {
        XFormParser parser = new XFormParser(new FileReader("resources/" + formName));
        return parser.parse();
    }
}
