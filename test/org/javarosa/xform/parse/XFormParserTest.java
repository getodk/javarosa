package org.javarosa.xform.parse;

import org.javarosa.core.model.FormDef;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.io.*;

public class XFormParserTest {
    @Test public void parsesSimpleForm() throws IOException {
        XFormParser parser = new XFormParser(new FileReader("resources/simple-form.xml"));
        FormDef formDef = parser.parse();
        assertEquals(formDef.getTitle(), "Simple Form");
    }
}
