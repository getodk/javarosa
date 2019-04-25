package org.javarosa.xform.parse;

import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.SubmissionProfile;
import org.javarosa.core.model.test.DummyReference;
import org.junit.Test;
import org.kxml2.kdom.Element;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class SubmissionParserTest {

    @Test public void parseSubmission() throws Exception {
        // Given
        final SubmissionParser submissionParser = new SubmissionParser();

        final Element element = new Element();
        element.setAttribute(null, "mediatype", "application/xml");
        element.setAttribute(null, "custom_attribute", "custom value");

        final String method = "POST";
        final String action = "ACTION";
        final IDataReference ref = new DummyReference();

        // When
        final SubmissionProfile submissionProfile = submissionParser.parseSubmission(method, action, ref, element);

        // Then
        assertEquals(action, submissionProfile.getAction());
        assertEquals(method, submissionProfile.getMethod());
        assertEquals(ref, submissionProfile.getRef());
        assertEquals("application/xml", submissionProfile.getMediaType());
        assertEquals("custom value", submissionProfile.getAttribute("custom_attribute"));
    }

    @Test public void matchesCustomMethod() throws Exception {
        assertFalse(new SubmissionParser().matchesCustomMethod("stub"));
    }

}