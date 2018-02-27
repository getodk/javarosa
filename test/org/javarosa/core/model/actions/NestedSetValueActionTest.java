package org.javarosa.core.model.actions;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.data.DateTimeData;
import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.TreeReference;
import org.joda.time.DateTimeUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.javarosa.xform.parse.FormParserHelper.parse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class NestedSetValueActionTest {

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
        final FormDef formDef =
                parse(r("nested-setvalue-action.xml")).formDef;

        TreeReference triggerRef = new TreeReference();
        triggerRef.setRefLevel(TreeReference.REF_ABSOLUTE);
        triggerRef.add("data", 0);
        triggerRef.add("cost", 0);

        TreeReference targetRef = new TreeReference();
        targetRef.setRefLevel(TreeReference.REF_ABSOLUTE);
        targetRef.add("data", 0);
        targetRef.add("cost_timestamp", 0);

        IAnswerData targetValue = formDef.getMainInstance().resolveReference(targetRef).getValue();
        assertNull(targetValue);

        // When
        formDef.setValue(new DecimalData(22.0), triggerRef, true);

        // Then
        targetValue = formDef.getMainInstance().resolveReference(targetRef).getValue();
        IAnswerData expectedValue = new DateTimeData(new Date(DATE_NOW));

        assertEquals(expectedValue.getValue(), targetValue.getValue());
    }

    @Test
    public void when_triggerNodeIsUpdatedWithTheSameValue_targetNodeCalculation_isNotEvaluated() throws IOException {
        // Given
        final FormDef formDef =
                parse(r("nested-setvalue-action.xml")).formDef;

        TreeReference triggerRef = new TreeReference();
        triggerRef.setRefLevel(TreeReference.REF_ABSOLUTE);
        triggerRef.add("data", 0);
        triggerRef.add("cost", 0);

        TreeReference targetRef = new TreeReference();
        targetRef.setRefLevel(TreeReference.REF_ABSOLUTE);
        targetRef.add("data", 0);
        targetRef.add("cost_timestamp", 0);

        DecimalData decimalValue = new DecimalData(22.0);
        formDef.getMainInstance().resolveReference(targetRef).setValue(new DateTimeData(new Date(DATE_NOW)));
        formDef.getMainInstance().resolveReference(triggerRef).setValue(decimalValue);

        // When
        DateTimeUtils.setCurrentMillisFixed(DATE_NOW + DAY_OFFSET); // shift the current time so we can test whether the setvalue action was re-fired
        formDef.setValue(decimalValue, triggerRef, true);

        // Then
        IAnswerData targetValue = formDef.getMainInstance().resolveReference(targetRef).getValue();
        IAnswerData expectedValue = new DateTimeData(new Date(DATE_NOW));

        assertEquals(expectedValue.getValue(), targetValue.getValue());
    }

}