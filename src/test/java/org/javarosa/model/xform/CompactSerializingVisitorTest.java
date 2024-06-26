package org.javarosa.model.xform;

import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.services.transport.payload.ByteArrayPayload;
import org.javarosa.test.FormParseInit;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.xform.parse.XFormParser;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.javarosa.test.ResourcePathHelper.r;
import static org.javarosa.xform.parse.XFormParser.NAMESPACE_ODK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Performs a comprehensive suite of tests on the results of a form
 * being serialized to the compact format.
 */
public class CompactSerializingVisitorTest {
    private String text;
    private FormInstance formInstance;

    @Before
    public void setUp() throws IOException, XFormParser.ParseException {
        FormParseInit formParser = new FormParseInit(r("sms_form.xml"));
        FormEntryController formEntryController = formParser.getFormEntryController();
        formInstance = formEntryController.getModel().getForm().getInstance();

        CompactSerializingVisitor serializer = new CompactSerializingVisitor();

        ByteArrayPayload payload = (ByteArrayPayload) serializer.createSerializedPayload(formInstance);

        text = payload.toString().replace("\\", "").replace("\\\\", "\\");
    }

    @Test
    public void ensurePrefixIsPresent() {
        TreeElement root = formInstance.getRoot();
        String prefix = root.getAttributeValue(NAMESPACE_ODK, "prefix");
        assertTrue(text.contains(prefix));
    }

    @Test
    public void SmsNotNull() {
        assertNotNull(text);
    }

    @Test
    public void ensureCorrectSerialization() {
        assertEquals("Serialized form", "FORM232;FN;John;LN;Doe;DOB;2015-08-05;CN;Mary Doe;CN;Sara Doe;CN;Jim Doe;PIC;test_image.jpg;", text);
    }

    /**
     * Ensures that the answer for maiden_name, which doesn’t have the “tag” attribute, is not present
     */
    @Test
    public void ensureAnswerInNonTaggedElementNotPresent() {
        assertFalse(text.contains("Placeholder"));
    }

    /**
     * Ensures that “CTY”, which does have the “tag” attribute, but has no answer, is not present
     */
    @Test
    public void ensureTagWithNoAnswerNotPresent() {
        assertFalse(text.contains("CTY"));
    }

    @Test
    public void ensureThatFormWithNoSmsTagsIsEmpty() throws IOException, XFormParser.ParseException {
        FormParseInit formParser = new FormParseInit(r("simple-form.xml"));
        FormEntryController formEntryController = formParser.getFormEntryController();
        formInstance = formEntryController.getModel().getForm().getInstance();

        CompactSerializingVisitor serializer = new CompactSerializingVisitor();

        ByteArrayPayload payload = (ByteArrayPayload) serializer.createSerializedPayload(formInstance);

        assertEquals(payload.toString(), "");
    }
}
