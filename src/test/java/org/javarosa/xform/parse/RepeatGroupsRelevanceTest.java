package org.javarosa.xform.parse;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.test.FormParseInit;
import org.javarosa.form.api.FormEntryModel;
import org.junit.Test;

import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.junit.Assert.assertFalse;

public class RepeatGroupsRelevanceTest {

    @Test
    public void isRepeatRelevantTest() {
        FormParseInit fpi = new FormParseInit(r("repeat_groups_relevance.xml"));
        FormEntryModel formEntryModel = fpi.getFormEntryModel();
        FormDef formDef = fpi.getFormDef();

        formEntryModel.setQuestionIndex(formEntryModel.incrementIndex(formEntryModel.getFormIndex()));
        formEntryModel.setQuestionIndex(formEntryModel.incrementIndex(formEntryModel.getFormIndex()));

        // repeatable group made not relevant by the previous question
        assertFalse(formDef.isRepeatRelevant(formDef.getChildInstanceRef(formEntryModel.getFormIndex())));

        formEntryModel.setQuestionIndex(formEntryModel.incrementIndex(formEntryModel.getFormIndex()));
        formEntryModel.setQuestionIndex(formEntryModel.incrementIndex(formEntryModel.getFormIndex()));
        formEntryModel.setQuestionIndex(formEntryModel.incrementIndex(formEntryModel.getFormIndex()));

        // repeatable group marked as not relevant
        assertFalse(formDef.isRepeatRelevant(formDef.getChildInstanceRef(formEntryModel.getFormIndex())));

        formEntryModel.setQuestionIndex(formEntryModel.incrementIndex(formEntryModel.getFormIndex()));
        formEntryModel.setQuestionIndex(formEntryModel.incrementIndex(formEntryModel.getFormIndex()));
        formEntryModel.setQuestionIndex(formEntryModel.incrementIndex(formEntryModel.getFormIndex()));

        // empty repeatable group
        assertFalse(formDef.isRepeatRelevant(formDef.getChildInstanceRef(formEntryModel.getFormIndex())));
    }
}
