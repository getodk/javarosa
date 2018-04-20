package org.javarosa.model.xform;

import org.javarosa.core.PathConst;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.services.transport.payload.ByteArrayPayload;
import org.javarosa.core.test.FormParseInit;
import org.javarosa.form.api.FormEntryController;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.javarosa.test.utils.Utils.convertToString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

/**
 * Performs a comprehensive suite of tests on the results of a form
 * being serialized to the SMS format.
 */
public class SMSSerializingVisitorTest {
    private FormParseInit formParser;
    private String SMS;
    private static final String delimiter = " ";
    private FormEntryController formEntryController;
    private FormInstance formInstance;

    @Before
    public void setup() throws IOException {
        formParser = new FormParseInit();
        formParser.setFormToParse(new File(PathConst.getTestResourcePath(), "sms_form.xml").toString());
        formEntryController = formParser.getFormEntryController();
        formInstance = formEntryController.getModel().getForm().getInstance();

        SMSSerializingVisitor serializer = new SMSSerializingVisitor();

        ByteArrayPayload payload = (ByteArrayPayload) serializer.createSerializedPayload(formInstance);

        SMS = convertToString(payload.getPayloadStream());
        SMS = SMS.replace("\\", "").replace("\\\\", "\\");
    }

    @Test
    public void SmsNotNull() {
        assertNotNull(SMS);
    }

    @Test
    public void testTagsAreBeingSerialized() {

        for (String tag : getAnswerTags()) {
            assertTrue(SMS.contains(tag));
        }
    }

    /**
     * Checks to see if all answers for the selected tags exist. This
     * targets answers of various data types  and structures such as
     * groups.
     */
    @Test
    public void testTaggedAnswers() {
        for (String tag : getAnswerTags()) {
            assertTrue(taggedAnswerExists(tag, SMS));
        }
    }

    /***
     * Checks to see an answer that doesn't have a tag is present .
     * Placeholder  is an answer bounded to the <maiden_name> tag
     */
    @Test
    public void testAnswerWithoutATag() {
        assertFalse(SMS.contains("Placeholder"));
    }

    /***
     * Checks to see a tag that doesn't have an answer exists within
     * the SMS.
     * the CTY tag is attached to  the country  question.
     */
    @Test
    public void testTagWithNoAnswer() {
        assertFalse(SMS.contains("CTY"));
    }

    /**
     * returns a list of all the tags that are bounded to
     * nodes that have answers.
     */
    private static List<String> getAnswerTags() {
        List<String> tags = new ArrayList<>();

        tags.add("FN");
        tags.add("LN");
        tags.add("DOB");
        tags.add("CN");
        tags.add("PIC");

        return tags;
    }

    /**
     * This function  checks to see if  a  tagged node within the
     * SMS has a corresponding answer by counting characters until the
     * delimiter is reached.
     *
     * @param tag
     * @param sms
     * @return true if  an answer exists after a tag.
     */
    private static boolean taggedAnswerExists(String tag, String sms) {

        if (!sms.contains(tag)) {
            return false;
        }

        String answer = "";
        int currentIndex;
        int delimiterSize = 1;

        Pattern pattern = Pattern.compile(tag);
        Matcher matcher = pattern.matcher(sms);

        /**
         * Check for  all occurrences of a tag. A matcher is used because
         *  a tag can be reused multiple times so each instance of that tag
         *  is discovered and the answer  it has gets checked.
         */
        while (matcher.find()) {
            currentIndex = delimiterSize + matcher.end();

            // loops while the next set of characters isn't a delimiter and the start of a next tag
            while (!sms.substring(currentIndex, currentIndex + 2).equals(delimiter + "+")) {
                answer += sms.substring(currentIndex, currentIndex + 1);

                currentIndex++;

                //breaks once we have almost reached the end of the sms
                if (currentIndex == sms.length() - 1) {

                    //appends the final character since the substring method
                    //of while condition  checks 2 indexes ahead.
                    answer += sms.substring(sms.length() - 1, sms.length());

                    break;
                }
            }
        }

        //Prints each tagged answer to the console.
        //System.out.println(answer);

        // returns true once the answer extracted from a tag is present.
        return answer.trim().length() > 0;
    }
}
