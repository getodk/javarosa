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
    public void answerDataToInt_returnsCorrectValueForIntegerData() {
        assertEquals(1, AnswerDataUtil.answerDataToInt(new IntegerData(1)));
        assertEquals(5, AnswerDataUtil.answerDataToInt(new IntegerData(5)));
        assertEquals(20, AnswerDataUtil.answerDataToInt(new IntegerData(20)));
    }

    @Test
    public void answerDataToInt_returnsZeroForIntegerDataIfThereIsNoValue() {
        assertEquals(0, AnswerDataUtil.answerDataToInt(new IntegerData()));
    }

    @Test
    public void answerDataToInt_returnsCorrectValueForDecimalData() {
        assertEquals(2, AnswerDataUtil.answerDataToInt(new DecimalData(2)));
        assertEquals(7, AnswerDataUtil.answerDataToInt(new DecimalData(7.5)));
        assertEquals(41, AnswerDataUtil.answerDataToInt(new DecimalData(41)));
    }

    @Test
    public void answerDataToInt_returnsZeroForDecimalDataIfThereIsNoValue() {
        assertEquals(0, AnswerDataUtil.answerDataToInt(new DecimalData()));
    }

    @Test
    public void answerDataToInt_returnsCorrectValueForLongData() {
        assertEquals(4, AnswerDataUtil.answerDataToInt(new LongData(4L)));
        assertEquals(15, AnswerDataUtil.answerDataToInt(new LongData(15L)));
        assertEquals(120, AnswerDataUtil.answerDataToInt(new LongData(120L)));
    }

    @Test
    public void answerDataToInt_returnsZeroForLongDataIfThereIsNoValue() {
        assertEquals(0, AnswerDataUtil.answerDataToInt(new LongData()));
    }

    @Test
    public void answerDataToInt_returnsCorrectValueForStringData() {
        assertEquals(6, AnswerDataUtil.answerDataToInt(new StringData("6")));
        assertEquals(9, AnswerDataUtil.answerDataToInt(new StringData("9.0")));
        assertEquals(17, AnswerDataUtil.answerDataToInt(new StringData("17")));
        assertEquals(21, AnswerDataUtil.answerDataToInt(new StringData("21.5")));
        assertEquals(53, AnswerDataUtil.answerDataToInt(new StringData("53")));
    }

    @Test
    public void answerDataToInt_returnsZeroForStringDataIfThereIsNoValue() {
        assertEquals(0, AnswerDataUtil.answerDataToInt(new StringData()));
    }

    @Test
    public void answerDataToInt_returnsZeroForStringDataIfTheValueCanNotBeConvertedToInteger() {
        assertEquals(0, AnswerDataUtil.answerDataToInt(new StringData("blah")));
    }

    @Test
    public void answerDataToInt_returnsZeroForNotSupportedDataTypes() {
        assertEquals(0, AnswerDataUtil.answerDataToInt(new BooleanData(true)));
        assertEquals(0, AnswerDataUtil.answerDataToInt(new SelectOneData(new Selection("Selection1"))));
        assertEquals(0, AnswerDataUtil.answerDataToInt(new DateData(new Date())));
    }
}
