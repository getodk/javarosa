package org.javarosa.model.xform;

import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.services.transport.payload.ByteArrayPayload;
import org.javarosa.core.test.FormParseInit;
import org.javarosa.form.api.FormEntryController;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Performs a comprehensive suite of tests on the results of a form
 * being serialized to the SMS format.
 */
public class SmsSerializingVisitorTest {
    private String sms;

    @Before
    public void setup() throws IOException {
        FormParseInit formParser = new FormParseInit();
        formParser.setFormToParse(r("sms_form.xml").toString());
        FormEntryController formEntryController = formParser.getFormEntryController();
        FormInstance formInstance = formEntryController.getModel().getForm().getInstance();

        SMSSerializingVisitor serializer = new SMSSerializingVisitor();

        ByteArrayPayload payload = (ByteArrayPayload) serializer.createSerializedPayload(formInstance);

        sms = new String(payload.getPayloadBytes(), "UTF-8").replace("\\", "").replace("\\\\", "\\");
    }

    @Test
    public void SmsNotNull() {
        assertNotNull(sms);
    }

    @Test
    public void ensureAllTagsArePresentAndHaveValues() {
        Set<String> tagsFound = new HashSet<>();
        String tags = "FN|LN|DOB|CN|PIC";
        Pattern p = Pattern.compile("(" + tags + ")\\s*(\\S+)");
        Matcher m = p.matcher(sms);
        while (m.find()) {
            if (m.groupCount() == 2) {
                tagsFound.add(m.group(1));
            }
        }
        assertEquals("Only these tags were found: " + tagsFound,
            tags.split("\\|").length, tagsFound.size());

    }

    /***
     * Checks to see than an answer doesn't have a tag is present.
     * Placeholder is an answer bounded to the <maiden_name> tag
     */
    @Test
    public void testAnswerWithoutATag() {
        assertFalse(sms.contains("Placeholder"));
    }

    /***
     * Checks to see a tag that doesn't have an answer exists within
     * the SMS.
     * the CTY tag is attached to the country question.
     */
    @Test
    public void testTagWithNoAnswer() {
        assertFalse(sms.contains("CTY"));
    }
}
