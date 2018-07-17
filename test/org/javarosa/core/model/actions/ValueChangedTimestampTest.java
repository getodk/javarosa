package org.javarosa.core.model.actions;

import org.javarosa.core.model.CoreModelModule;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.data.DateTimeData;
import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.core.services.PrototypeManager;
import org.javarosa.core.util.JavaRosaCoreModule;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.model.xform.XFormsModule;
import org.joda.time.DateTimeUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import static org.javarosa.core.util.externalizable.ExtUtil.defaultPrototypes;
import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.javarosa.xform.parse.FormParserHelper.parse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ValueChangedTimestampTest {

    private static final long DATE_NOW = 1_500_000_000_000L;
    private static final long DAY_OFFSET = 86_400_000L;

    @Before
    public void before() {
        DateTimeUtils.setCurrentMillisFixed(DATE_NOW);
    }

    @After
    public void after() {
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    public void when_triggerNodeIsUpdated_targetNodeCalculation_isEvaluated() throws IOException {
        // Given
        final FormDef formDef = parse(r("nested-setvalue-action.xml")).formDef;
        FormEntryController formEntryController = new FormEntryController(new FormEntryModel(formDef));
        TreeReference targetRef = createTargetRef(null);

        assertNullAt(formDef, targetRef);

        // When
        formEntryController.stepToNextEvent();
        formEntryController.answerQuestion(new DecimalData(22.0), true);

        // Then
        assertEquals(getDateValue(DATE_NOW), getValueAt(formDef, targetRef));
    }

    @Test
    public void when_triggerNodeIsUpdatedWithinRepeat_targetNodeCalculation_isEvaluated() throws IOException {
        // Given
        final FormDef formDef = parse(r("nested-setvalue-action-with-repeats.xml")).formDef;
        FormEntryController formEntryController = new FormEntryController(new FormEntryModel(formDef));

        TreeReference[] targetRefs = new TreeReference[3];

        for (int i = 0; i < targetRefs.length; i++) {
            targetRefs[i] = createTargetRef(i);;
            assertNullAt(formDef, targetRefs[i]);
        }

        // When
        for (int i = 0; i < targetRefs.length; i++) {
            int eventType = -1;
            while (eventType != FormEntryController.EVENT_QUESTION) {
                eventType = formEntryController.stepToNextEvent();
            }

            DateTimeUtils.setCurrentMillisFixed(todayPlusDays(i));
            formEntryController.answerQuestion(new DecimalData(i + 1), true);
        }

        // Then
        for (int i = 0; i < targetRefs.length; i++) {
            assertEquals(getDateValue(todayPlusDays(i)), getValueAt(formDef, targetRefs[i]));
        }
    }

    @Test
    public void when_triggerNodeIsUpdatedWithTheSameValue_targetNodeCalculation_isNotEvaluated() throws IOException {
        // Given
        final FormDef formDef = parse(r("nested-setvalue-action.xml")).formDef;
        FormEntryController formEntryController = new FormEntryController(new FormEntryModel(formDef));
        TreeReference targetRef = createTargetRef(null);

        formEntryController.stepToNextEvent();
        formEntryController.answerQuestion(new DecimalData(22.0), true);

        assertEquals(getDateValue(DATE_NOW), getValueAt(formDef, targetRef));

        // When
        DateTimeUtils.setCurrentMillisFixed(DATE_NOW + DAY_OFFSET); // shift the current time so we can test whether the setvalue action was re-fired
        formEntryController.answerQuestion(new DecimalData(22.0), true);

        // Then
        assertEquals(getDateValue(DATE_NOW), getValueAt(formDef, targetRef));
    }

    @Test
    public void testSerializationAndDeserialization() throws IOException, DeserializationException {
        PrototypeManager.registerPrototypes(JavaRosaCoreModule.classNames);
        PrototypeManager.registerPrototypes(CoreModelModule.classNames);
        new XFormsModule().registerModule();

        FormDef formDef = parse(r("nested-setvalue-action.xml")).formDef;
        Path p = Files.createTempFile("serialized-form", null);

        final DataOutputStream dos = new DataOutputStream(Files.newOutputStream(p));
        formDef.writeExternal(dos);
        dos.close();

        final DataInputStream dis = new DataInputStream(Files.newInputStream(p));
        formDef.readExternal(dis, defaultPrototypes());
        dis.close();

        Files.delete(p);

        TreeReference triggerRef = new TreeReference();
        triggerRef.setRefLevel(TreeReference.REF_ABSOLUTE);
        triggerRef.add("data", 0);
        triggerRef.add("cost", 0);

        TreeReference targetRef = createTargetRef(null);

        assertNullAt(formDef, targetRef);

        // When
        formDef.setValue(new DecimalData(22.0), triggerRef, true);

        // Then
        assertEquals(getDateValue(DATE_NOW), getValueAt(formDef, targetRef));
    }

    private TreeReference createTargetRef(Integer optionalRepeatIndex) {
        TreeReference targetRef = new TreeReference();
        targetRef.setRefLevel(TreeReference.REF_ABSOLUTE);
        targetRef.add("data", 0);
        if (optionalRepeatIndex != null)
            targetRef.add("repeat", optionalRepeatIndex);
        targetRef.add("cost_timestamp", 0);
        return targetRef;
    }

    private long todayPlusDays(int i) {
        return DATE_NOW + i * DAY_OFFSET;
    }

    private void assertNullAt(FormDef formDef, TreeReference targetRef) {
        assertNull(getAnswerDataAt(formDef, targetRef));
    }

    private Object getDateValue(long date) {
        return new DateTimeData(new Date(date)).getValue();
    }

    private Object getValueAt(FormDef formDef, TreeReference targetRef) {
        return getAnswerDataAt(formDef, targetRef).getValue();
    }

    private IAnswerData getAnswerDataAt(FormDef formDef, TreeReference targetRef) {
        return formDef.getMainInstance().resolveReference(targetRef).getValue();
    }
}
