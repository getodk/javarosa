package org.javarosa.core.model.test;

import static org.javarosa.test.ResourcePathHelper.r;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.test.FormParseInit;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.xform.parse.XFormParser;
import org.junit.Test;

public class FormIndexSerializationTest {

    @Test
    public void testBeginningOfForm() throws IOException, ClassNotFoundException {
        FormIndex formIndexToSerialize = FormIndex.createBeginningOfFormIndex();
        byte[] serializedObject = serializeObject(formIndexToSerialize);
        FormIndex formIndexDeserialized = deserializeFormIndex(serializedObject);
        assertFormIndex(formIndexToSerialize, formIndexDeserialized);
    }

    @Test
    public void testEndOfForm() throws IOException, ClassNotFoundException {
        FormIndex formIndexToSerialize = FormIndex.createEndOfFormIndex();
        byte[] serializedObject = serializeObject(formIndexToSerialize);
        FormIndex formIndexDeserialized = deserializeFormIndex(serializedObject);
        assertFormIndex(formIndexToSerialize, formIndexDeserialized);
    }

    @Test
    public void testLocalAndInstanceNullReference() throws IOException, ClassNotFoundException {
        FormIndex formIndexToSerialize = new FormIndex(1, 2, null);
        byte[] serializedObject = serializeObject(formIndexToSerialize);
        FormIndex formIndexDeserialized = deserializeFormIndex(serializedObject);
        assertFormIndex(formIndexToSerialize, formIndexDeserialized);
    }

    @Test
    public void testLocalAndInstanceNonNullReference() throws IOException, ClassNotFoundException {
        TreeReference treeReference = TreeReference.rootRef();
        FormIndex formIndexToSerialize = new FormIndex(1, 2, treeReference);
        byte[] serializedObject = serializeObject(formIndexToSerialize);
        FormIndex formIndexDeserialized = deserializeFormIndex(serializedObject);
        assertFormIndex(formIndexToSerialize, formIndexDeserialized);
    }

    @Test
    public void testOnFormController() throws IOException, ClassNotFoundException, XFormParser.ParseException {
        FormParseInit formParseInit = new FormParseInit(r("formindex-serialization.xml"));
        FormEntryController formEntryController = formParseInit.getFormEntryController();

        FormIndex formIndex = formEntryController.getModel().getFormIndex();
        assertFormIndex(formIndex, deserializeFormIndex(serializeObject(formIndex)));

        do {
            formIndex = formEntryController.getModel().incrementIndex(formIndex);
            assertFormIndex(formIndex, deserializeFormIndex(serializeObject(formIndex)));
        } while (!formIndex.isEndOfFormIndex());
    }

    // helper methods -->

    private static byte[] serializeObject(Serializable serializable)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(serializable);
        oos.close();
        return baos.toByteArray();
    }

    private static FormIndex deserializeFormIndex(byte[] serializedFormIndex)
            throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(serializedFormIndex);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object o = ois.readObject();
        return (FormIndex) o;
    }

    // Fails if expected FormIndex is not equal to actual FormIndex.
    private static void assertFormIndex(FormIndex expected, FormIndex actual) {
        assertEquals(expected, actual);
        assertEquals(expected.getReference(), actual.getReference());
    }
}