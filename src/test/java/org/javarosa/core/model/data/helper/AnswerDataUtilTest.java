package org.javarosa.core.model.data.helper;

import org.javarosa.core.model.data.BooleanData;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.LongData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.StringData;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class AnswerDataUtilTest {

    @Test
    public void NumericalDataReturnsProperValueWhenConvertedToInt() {
        assertEquals(5, AnswerDataUtil.answerDataToInt(new IntegerData(5)));

        assertEquals(7, AnswerDataUtil.answerDataToInt(new DecimalData(7.35)));

        assertEquals(3, AnswerDataUtil.answerDataToInt(new LongData(3L)));
    }

    @Test
    public void NonNumericalDataReturnsZeroWhenConvertedToInt() {
        assertEquals(0, AnswerDataUtil.answerDataToInt(new BooleanData(true)));
        assertEquals(0, AnswerDataUtil.answerDataToInt(new SelectOneData(new Selection("Selection1"))));
        assertEquals(0, AnswerDataUtil.answerDataToInt(new DateData(new Date())));
        assertEquals(0, AnswerDataUtil.answerDataToInt(new StringData("blah")));
    }

    @Test
    public void IfDataTypeHasNoAnswerReturnsZeroWhenConvertedToInt() {
        assertEquals(0, AnswerDataUtil.answerDataToInt(new IntegerData()));
        assertEquals(0, AnswerDataUtil.answerDataToInt(new DecimalData()));
        assertEquals(0, AnswerDataUtil.answerDataToInt(new LongData()));
        assertEquals(0, AnswerDataUtil.answerDataToInt(new BooleanData()));
    }
}
