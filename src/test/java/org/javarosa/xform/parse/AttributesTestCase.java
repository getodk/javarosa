package org.javarosa.xform.parse;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.test.FormParseInit;
import org.javarosa.test.Scenario;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Test;

import java.io.IOException;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.javarosa.test.BindBuilderXFormsElement.bind;
import static org.javarosa.test.ResourcePathHelper.r;
import static org.javarosa.test.XFormsElement.body;
import static org.javarosa.test.XFormsElement.data;
import static org.javarosa.test.XFormsElement.head;
import static org.javarosa.test.XFormsElement.html;
import static org.javarosa.test.XFormsElement.input;
import static org.javarosa.test.XFormsElement.label;
import static org.javarosa.test.XFormsElement.mainInstance;
import static org.javarosa.test.XFormsElement.model;
import static org.javarosa.test.XFormsElement.t;
import static org.javarosa.test.XFormsElement.title;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
    public void inputClientAttributes_arePassedThroughWithoutWarnings() throws IOException, XFormParser.ParseException, DeserializationException {
        Scenario scenario = Scenario.init(html(
            head(title(), model(
                mainInstance(data(t("text"), t("location"))),
                bind("/data/text").type("string"),
                bind("/data/location").type("geopoint")
            )),
            body(
                t("input ref=\"/data/text\" rows=\"5\" query=\"some-query\" autoplay=\"audio\" playColor=\"red\"", label("Text")),
                t("input ref=\"/data/location\" accuracyThreshold=\"4\"", label("Location"))
            )
        ));

        for (FormDef formDef : asList(scenario.getFormDef(), scenario.serializeAndDeserializeForm().getFormDef())) {
            assertEquals("5", formDef.getChild(0).getAdditionalAttribute(null, "rows"));
            assertEquals("some-query", formDef.getChild(0).getAdditionalAttribute(null, "query"));
            assertEquals("audio", formDef.getChild(0).getAdditionalAttribute(null, "autoplay"));
            assertEquals("red", formDef.getChild(0).getAdditionalAttribute(null, "playColor"));
            assertEquals("4", formDef.getChild(1).getAdditionalAttribute(null, "accuracyThreshold"));
        }
        assertTrue(scenario.getFormDef().getParseWarnings().toString(), scenario.getFormDef().getParseWarnings().isEmpty());
    }

    @Test
    public void groupIntent_isPassedThroughWithoutWarnings() throws IOException, XFormParser.ParseException, DeserializationException {
        String intent = "org.mycompany.myapp(my_text='Some text',uuid=/data/meta/instanceID)";
        Scenario scenario = Scenario.init(html(
            head(title(), model(mainInstance(data(t("group", t("text")))))),
            body(t("group ref=\"/data/group\" intent=\"" + intent + "\"",
                input("/data/group/text", label("Text"))
            ))
        ));

        for (FormDef formDef : asList(scenario.getFormDef(), scenario.serializeAndDeserializeForm().getFormDef())) {
            assertEquals(intent, formDef.getChild(0).getAdditionalAttribute(null, "intent"));
        }
        assertTrue(scenario.getFormDef().getParseWarnings().toString(), scenario.getFormDef().getParseWarnings().isEmpty());
    }

    @Test
    public void unknownAttributes_areStillPassedThroughWithWarnings() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init(html(
            head(title(), model(mainInstance(data(t("group", t("text")))))),
            body(t("group ref=\"/data/group\" unknownGroup=\"group-value\"",
                t("input ref=\"/data/group/text\" unknownInput=\"input-value\"", label("Text"))
            ))
        ));
        FormDef formDef = scenario.getFormDef();

        assertEquals("group-value", formDef.getChild(0).getAdditionalAttribute(null, "unknownGroup"));
        assertEquals("input-value", formDef.getChild(0).getChild(0).getAdditionalAttribute(null, "unknownInput"));
        assertEquals(2, formDef.getParseWarnings().size());
        assertThat(formDef.getParseWarnings(), hasItem(containsString("Unrecognized attributes found in Element [input] and will be ignored: [unknownInput]")));
        assertThat(formDef.getParseWarnings(), hasItem(containsString("Unrecognized attributes found in Element [group] and will be ignored: [unknownGroup]")));
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
