package org.javarosa.core.form.api.test;


import org.javarosa.core.PathConst;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.test.FormParseInit;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Test;

import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LimitedRepeatCountTests {

    private static final int TEN_REPEAT_COUNT = 10;
    private static final int NINE_REPEAT_COUNT = 9;
    private static final int FIVE_REPEAT_COUNT = 5;

    @Test
    public void testRemoveExtraneousRepeatGroups() {
        FormParseInit formParseInit = new FormParseInit();
        formParseInit.setFormToParse(Paths.get(PathConst.getTestResourcePath().getAbsolutePath(), "limited-repeats-count.xml").toString());

        FormEntryController formEntryController = formParseInit.getFormEntryController();

        assertTrue(formEntryController.getModel().getFormIndex().isBeginningOfFormIndex());

        formEntryController.stepToNextEvent();

        FormEntryPrompt questionPrompt = formEntryController.getModel().getQuestionPrompt();

        assertEquals("/data/repeat_count", questionPrompt.getQuestion().getBind().getReference().toString());
        formEntryController.answerQuestion(questionPrompt.getIndex(), new IntegerData(TEN_REPEAT_COUNT), true);

        for (int i = 0; i < TEN_REPEAT_COUNT; i++) {
            assertEquals(FormEntryController.EVENT_REPEAT, formEntryController.stepToNextEvent());
            assertEquals(FormEntryController.EVENT_QUESTION, formEntryController.stepToNextEvent());
            assertEquals(FormEntryController.EVENT_QUESTION, formEntryController.stepToNextEvent());
        }
        assertEquals(FormEntryController.EVENT_END_OF_FORM, formEntryController.stepToNextEvent());

        formEntryController.jumpToIndex(questionPrompt.getIndex());
        assertEquals("/data/repeat_count", questionPrompt.getQuestion().getBind().getReference().toString());
        formEntryController.answerQuestion(questionPrompt.getIndex(), new IntegerData(NINE_REPEAT_COUNT), true);

        for (int i = 0; i < NINE_REPEAT_COUNT; i++) {
            assertEquals(FormEntryController.EVENT_REPEAT, formEntryController.stepToNextEvent());
            assertEquals(FormEntryController.EVENT_QUESTION, formEntryController.stepToNextEvent());
            assertEquals(FormEntryController.EVENT_QUESTION, formEntryController.stepToNextEvent());
        }
        assertEquals(FormEntryController.EVENT_END_OF_FORM, formEntryController.stepToNextEvent());

        formEntryController.jumpToIndex(questionPrompt.getIndex());
        assertEquals("/data/repeat_count", questionPrompt.getQuestion().getBind().getReference().toString());
        formEntryController.answerQuestion(questionPrompt.getIndex(), new IntegerData(FIVE_REPEAT_COUNT), true);

        for (int i = 0; i < FIVE_REPEAT_COUNT; i++) {
            assertEquals(FormEntryController.EVENT_REPEAT, formEntryController.stepToNextEvent());
            assertEquals(FormEntryController.EVENT_QUESTION, formEntryController.stepToNextEvent());
            assertEquals(FormEntryController.EVENT_QUESTION, formEntryController.stepToNextEvent());
        }
        assertEquals(FormEntryController.EVENT_END_OF_FORM, formEntryController.stepToNextEvent());
    }

}
