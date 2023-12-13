package org.javarosa.form.api;

import org.javarosa.core.test.FormParseInit;
import org.javarosa.xform.parse.XFormParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.javarosa.form.api.FormEntryController.EVENT_PROMPT_NEW_REPEAT;
import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class FormNavigationTestCase {

    private final String formName;
    private final String[] expectedIndices;

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        // Expected indices when increasing indexes until the end of the form.
        // An index of -1 indicates the start or end of a form.
        return Arrays.asList(
                ei("repeatGroupWithTwoQuestions.xml",
                        "-1, ", "0_0, ", "0_0, 0, ", "0_0, 1, ", "0_1, ", "0_1, 0, ",
                        "0_1, 1, ", "0_2, ", "0_2, 0, ", "0_2, 1, ", "0_3, ", "-1, "),

                ei("repeatGroupWithQuestionAndRegularGroupInside.xml",
                        "-1, ", "0_0, ", "0_0, 0, ", "0_0, 1, ", "0_0, 1, 0, ", "0_0, 1, 1, ",
                        "0_1, ", "0_1, 0, ", "0_1, 1, ", "0_1, 1, 0, ", "0_1, 1, 1, ", "0_2, ", "0_2, 0, ",
                        "0_2, 1, ", "0_2, 1, 0, ", "0_2, 1, 1, ", "0_3, ", "-1, "),

                ei("twoNestedRegularGroups.xml",
                        "-1, ", "0, ", "0, 0, ", "0, 0, 0, ", "0, 0, 1, ", "-1, "),

                ei("twoNestedRepeatGroups.xml",
                        "-1, ", "0_0, ", "0_0, 0_0, ", "0_0, 0_0, 0, ", "0_0, 0_0, 1, ", "0_0, 0_1, ",
                        "0_0, 0_1, 0, ", "0_0, 0_1, 1, ", "0_0, 0_2, ",
                        //For #4059 fix
                        "0_0, 0_2, 0, ", "0_0, 0_2, 1, ", "0_0, 0_3, ", "0_1, ", "-1, "),

                ei("simpleFormWithThreeQuestions.xml",
                        "-1, ", "0, ", "1, ", "2, ", "-1, ")
        );
    }

    /** Expected indices for each form */
    private static Object[] ei(String formName, String... expectedIndices) {
        return new Object[] {formName, expectedIndices};
    }

    public FormNavigationTestCase(String formName, String[] expectedIndices) {
        this.formName = formName;
        this.expectedIndices = expectedIndices;
    }

    @Test
    public void formNavigationTestCase() throws XFormParser.ParseException {
        testIndices();
    }

    @Test
    // For each form, simulate increasing the index until the end of the
    // form and then decreasing until the beginning of the form.
    // Verify the expected indices before and after each operation.
    public void testIndices() throws XFormParser.ParseException {
        FormParseInit fpi = new FormParseInit(r("navigation/" + formName));
        FormEntryController formEntryController = fpi.getFormEntryController();
        FormEntryModel formEntryModel = fpi.getFormEntryModel();

        int repeatCount = 0;
        for (int i = 0; i < expectedIndices.length - 1; i++) { // navigate forwards
            // check the current index
            assertEquals(expectedIndices[i], formEntryModel.getFormIndex().toString());
            if (repeatCount < 3 && formEntryController.getModel().getEvent() == EVENT_PROMPT_NEW_REPEAT) {
                formEntryController.newRepeat();
                repeatCount++;
            }
            formEntryModel.setQuestionIndex(formEntryModel.incrementIndex(formEntryModel.getFormIndex()));
            // check the index again after increasing the index
            assertEquals(expectedIndices[i + 1], formEntryModel.getFormIndex().toString());
        }

        for (int i = expectedIndices.length - 1; i > 0 ; i--) { // navigate backwards
            // check the current index
            assertEquals(expectedIndices[i], formEntryModel.getFormIndex().toString());
            formEntryModel.setQuestionIndex(formEntryModel.decrementIndex(formEntryModel.getFormIndex()));
            // check the index again after decreasing the index
            assertEquals(expectedIndices[i - 1], formEntryModel.getFormIndex().toString());
        }
    }
}
