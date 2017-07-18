package org.javarosa.xform.parse;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.RangeQuestion;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.services.transport.payload.ByteArrayPayload;
import org.javarosa.model.xform.XFormSerializingVisitor;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.Files.copy;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.javarosa.core.model.Constants.CONTROL_RANGE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class XFormParserTest {

    private static final String FORM_INSTANCE_XML_FILE_NAME = "instance.xml";
    private static final String AUDIT_NODE = "audit";
    private static final String AUDIT_ANSWER = "audit111.csv";

    private static final String AUDIT_2_NODE = "audit2";
    private static final String AUDIT_2_ANSWER = "audit222.csv";

    private static final String AUDIT_3_NODE = "audit3";
    private static final String AUDIT_3_ANSWER = "audit333.csv";

    @After
    public void tearDown() throws Exception {
        new File(FORM_INSTANCE_XML_FILE_NAME).delete();
    }

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

    @Test
    public void serializeAndRestoreMetaNamespaceFormInstance() throws IOException {
        // Given
        ParseResult parseResult = parse("meta-namespace-form.xml");
        assertEquals(parseResult.formDef.getTitle(), "Namespace for Metadata");
        assertEquals("Number of error messages", 0, parseResult.errorMessages.size());

        FormDef formDef = parseResult.formDef;
        TreeElement audit = findDepthFirst(formDef.getInstance().getRoot(), AUDIT_NODE);
        TreeElement audit2 = findDepthFirst(formDef.getInstance().getRoot(), AUDIT_2_NODE);
        TreeElement audit3 = findDepthFirst(formDef.getInstance().getRoot(), AUDIT_3_NODE);
        assertNotNull(audit);
        assertNotNull(audit2);
        assertNotNull(audit3);
        audit.setAnswer(new StringData(AUDIT_ANSWER));
        audit2.setAnswer(new StringData(AUDIT_2_ANSWER));
        audit3.setAnswer(new StringData(AUDIT_3_ANSWER));

        // When

        // serialize the form instance
        XFormSerializingVisitor serializer = new XFormSerializingVisitor();
        ByteArrayPayload xml = (ByteArrayPayload) serializer.createSerializedPayload(formDef.getInstance());
        copy(xml.getPayloadStream(), new File(FORM_INSTANCE_XML_FILE_NAME).toPath(), REPLACE_EXISTING);

        // restore (deserialize) the form instance
        byte[] formInstanceBytes = readAllBytes(Paths.get(FORM_INSTANCE_XML_FILE_NAME));
        FormInstance formInstance = XFormParser.restoreDataModel(formInstanceBytes, null);

        // Then
        audit = findDepthFirst(formInstance.getRoot(), AUDIT_NODE);
        audit2 = findDepthFirst(formInstance.getRoot(), AUDIT_2_NODE);
        audit3 = findDepthFirst(formInstance.getRoot(), AUDIT_3_NODE);

        assertNotNull(audit);
        assertEquals(AUDIT_ANSWER, audit.getValue().getValue());

        assertNotNull(audit2);
        assertEquals(AUDIT_2_ANSWER, audit2.getValue().getValue());

        assertNotNull(audit3);
        assertEquals(AUDIT_3_ANSWER, audit3.getValue().getValue());
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

    private TreeElement findDepthFirst(TreeElement parent, String name) {
        int len = parent.getNumChildren();
        for (int i = 0; i < len; ++i) {
            TreeElement e = parent.getChildAt(i);
            if (name.equals(e.getName())) {
                return e;
            } else if (e.getNumChildren() != 0) {
                TreeElement v = findDepthFirst(e, name);
                if (v != null) {
                    return v;
                }
            }
        }
        return null;
    }
}
