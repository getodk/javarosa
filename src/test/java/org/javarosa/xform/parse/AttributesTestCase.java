package org.javarosa.xform.parse;

import org.javarosa.core.model.FormDef;
import org.javarosa.test.FormParseInit;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Test;

import static org.javarosa.test.ResourcePathHelper.r;
import static org.junit.Assert.assertEquals;

/**
 * Attributes that started being used by clients without being added as fields to DataBinding or QuestionDef should
 * be passed through and made available in the bindAttributes or additionalAttributes list.
 */
public class AttributesTestCase {

    @Test
    public void testBindAttributes() throws XFormParser.ParseException {
        FormParseInit fpi = new FormParseInit(r("form_with_bind_attributes.xml"));
        FormEntryModel formEntryModel = fpi.getFormEntryModel();
        FormDef formDef = fpi.getFormDef();
        FormEntryPrompt formEntryPrompt;
        formEntryModel.setQuestionIndex(formEntryModel.incrementIndex(formEntryModel.getFormIndex()));

        // First question - text
        formEntryPrompt = new FormEntryPrompt(formDef, formEntryModel.getFormIndex());
        assertEquals("requiredMsg", formEntryPrompt.getBindAttributes().get(0).getName());
        assertEquals("Custom required message", formEntryPrompt.getBindAttributes().get(0).getAttributeValue());
        assertEquals("saveIncomplete", formEntryPrompt.getBindAttributes().get(1).getName());
        assertEquals("true()", formEntryPrompt.getBindAttributes().get(1).getAttributeValue());
        formEntryModel.setQuestionIndex(formEntryModel.incrementIndex(formEntryModel.getFormIndex()));

        // Second question - image
        formEntryPrompt = new FormEntryPrompt(formDef, formEntryModel.getFormIndex());
        assertEquals("max-pixels", formEntryPrompt.getBindAttributes().get(0).getName());
        assertEquals("1500", formEntryPrompt.getBindAttributes().get(0).getAttributeValue());
    }

    @Test
    public void testAdditionalAttributes() throws XFormParser.ParseException {
        FormParseInit fpi = new FormParseInit(r("form_with_additional_attributes.xml"));
        FormEntryModel formEntryModel = fpi.getFormEntryModel();
        FormDef formDef = fpi.getFormDef();
        FormEntryPrompt formEntryPrompt;
        formEntryModel.setQuestionIndex(formEntryModel.incrementIndex(formEntryModel.getFormIndex()));

        // Group
        assertEquals("org.mycompany.myapp(my_text='Some text',uuid=/myform/meta/instanceID)", formEntryModel.getCaptionPrompt().getFormElement().getAdditionalAttribute(null, "intent"));
        formEntryModel.setQuestionIndex(formEntryModel.incrementIndex(formEntryModel.getFormIndex()));

        // First question - text
        formEntryPrompt = new FormEntryPrompt(formDef, formEntryModel.getFormIndex());
        assertEquals("5", formEntryPrompt.getQuestion().getAdditionalAttribute(null, "rows"));
        assertEquals("audio", formEntryPrompt.getQuestion().getAdditionalAttribute(null, "autoplay"));
        assertEquals("red", formEntryPrompt.getQuestion().getAdditionalAttribute(null, "playColor"));
        formEntryModel.setQuestionIndex(formEntryModel.incrementIndex(formEntryModel.getFormIndex()));

        // Second question - geopoint
        formEntryPrompt = new FormEntryPrompt(formDef, formEntryModel.getFormIndex());
        assertEquals("4", formEntryPrompt.getQuestion().getAdditionalAttribute(null, "accuracyThreshold"));
        formEntryModel.setQuestionIndex(formEntryModel.incrementIndex(formEntryModel.getFormIndex()));

        // Third question - itemset
        formEntryPrompt = new FormEntryPrompt(formDef, formEntryModel.getFormIndex());
        assertEquals("instance('counties')/root/item[state= /new_cascading_select/state ]", formEntryPrompt.getQuestion().getAdditionalAttribute(null, "query"));
    }
}
