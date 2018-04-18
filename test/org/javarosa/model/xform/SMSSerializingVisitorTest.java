package org.javarosa.model.xform;

import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.services.transport.payload.ByteArrayPayload;
import org.javarosa.core.test.FormParseInit;
import org.javarosa.form.api.FormEntryController;
import org.junit.Before;

import java.io.IOException;

import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.javarosa.test.utils.Utils.convertToString;

public class SMSSerializingVisitorTest {
    private FormParseInit formParser;
    private String SMS;
    private FormEntryController formEntryController;
    private FormInstance formInstance;

    @Before
    public void setup() throws IOException {
        formParser = new FormParseInit();
        formParser.setFormToParse(r("sms_form.xml").toString());
        formEntryController = formParser.getFormEntryController();
        formInstance = formEntryController.getModel().getForm().getInstance();

        SMSSerializingVisitor serializer = new SMSSerializingVisitor();

        ByteArrayPayload payload = (ByteArrayPayload) serializer.createSerializedPayload(formInstance);

        SMS = convertToString(payload.getPayloadStream());
    }
}
