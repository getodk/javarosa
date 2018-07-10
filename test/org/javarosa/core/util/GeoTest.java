package org.javarosa.core.util;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.InstanceInitializationFactory;
import org.javarosa.xpath.XPathUnhandledException;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.javarosa.xform.parse.FormParserHelper.parse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

abstract class GeoTest {
    /**
     * Parses a form, and fails the test if initializing the form doesn’t throw an
     * XPathUnhandledException with the expected message in the cause.
     *
     * @param formFilename the filename of the form
     * @param message the message that is expected in the exception
     * @throws IOException
     */
    static void expectUnhandledExceptionWithMessage(String formFilename, String message) throws IOException {
        FormDef formDef = parse(r(formFilename)).formDef;
        try {
            formDef.initialize(true, new InstanceInitializationFactory());
        } catch (Exception e) {
            if (e.getCause() instanceof XPathUnhandledException) {
                assertThat(e.getCause().getMessage(), containsString(message));
                return;
            }
        }
        fail("The expected exception was not thrown");
    }
}
