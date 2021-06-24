package org.javarosa.xform.parse;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.javarosa.core.reference.ReferenceManagerTestUtils.setUpSimpleReferenceManager;
import static org.javarosa.core.util.BindBuilderXFormsElement.bind;
import static org.javarosa.core.util.XFormsElement.body;
import static org.javarosa.core.util.XFormsElement.head;
import static org.javarosa.core.util.XFormsElement.html;
import static org.javarosa.core.util.XFormsElement.mainInstance;
import static org.javarosa.core.util.XFormsElement.model;
import static org.javarosa.core.util.XFormsElement.select1Dynamic;
import static org.javarosa.core.util.XFormsElement.t;
import static org.javarosa.core.util.XFormsElement.title;
import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.javarosa.xform.parse.FormParserHelper.deserializeAndCleanUpSerializedForm;
import static org.javarosa.xform.parse.FormParserHelper.getSerializedFormPath;
import static org.javarosa.xform.parse.FormParserHelper.parse;
import static org.javarosa.xpath.XPathParseTool.parseXPath;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.model.instance.AbstractTreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.test.FormParseInit;
import org.javarosa.core.test.Scenario;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.xpath.expr.XPathPathExpr;
import org.javarosa.xpath.parser.XPathSyntaxException;
import org.junit.Test;

public class ExternalSecondaryInstanceParseTest {
    @Test
    public void itemsFromExternalSecondaryXMLInstance_ShouldBeAvailableToXPathParser() throws IOException, XPathSyntaxException {
        Path formName = r("external-select-xml.xml");
        setUpSimpleReferenceManager(formName.getParent(), "file");
        FormDef formDef = parse(formName);
        assertEquals("XML External Secondary Instance", formDef.getTitle());

        TreeReference treeReference = ((XPathPathExpr) parseXPath("instance('external-xml')/root/item")).getReference();
        EvaluationContext evaluationContext = formDef.getEvaluationContext();
        List<TreeReference> treeReferences = evaluationContext.expandReference(treeReference);
        assertThat(treeReferences.size(), is(12));

        AbstractTreeElement fifthItem = formDef.getNonMainInstance("external-xml").resolveReference(treeReferences.get(4));
        assertThat(fifthItem.getChild("label", 0).getValue().getDisplayText(), is("AB"));
    }

    @Test
    public void xformParseException_whenItemsetConfiguresValueOrLabelNotInExternalInstance() throws IOException {
        setUpSimpleReferenceManager(r("external-select-csv.xml").getParent(), "file-csv");
        try {
            Scenario.init("Some form", html(
                head(
                    title("Some form"),
                    model(
                        mainInstance(t("data id=\"some-form\"",
                            t("first")
                        )),

                        t("instance id=\"external-csv\" src=\"jr://file-csv/external-data.csv\""),

                        bind("/data/first").type("string")
                    )
                ),
                body(
                    // Define a select using value and label references that don't exist in the secondary instance
                    select1Dynamic("/data/first", "instance('external-csv')/root/item", "foo", "bar")
                )));
            fail("Expected XFormParseException because itemset references don't exist in external instance");
        } catch (XFormParseException e) {
            // pass
        }
    }

    @Test
    public void formWithExternalSecondaryXMLInstance_ShouldSerializeAndDeserialize() throws IOException, DeserializationException {
        Path formPath = r("external-select-xml.xml");
        setUpSimpleReferenceManager(formPath.getParent(), "file");

        FormDef originalFormDef = parse(formPath);

        Path serializedForm = getSerializedFormPath(originalFormDef);
        FormDef deserializedFormDef = deserializeAndCleanUpSerializedForm(serializedForm);

        assertThat(originalFormDef.getTitle(), is(deserializedFormDef.getTitle()));
    }

    @Test
    public void deserializedFormDefCreatedFromAFormWithExternalSecondaryXMLInstance_ShouldContainThatExternalInstance() throws IOException, DeserializationException {
        Path formPath = r("external-select-xml.xml");
        setUpSimpleReferenceManager(formPath.getParent(), "file");

        FormDef originalFormDef = parse(formPath);
        originalFormDef.setFormXmlPath(formPath.toString());

        Path serializedForm = getSerializedFormPath(originalFormDef);
        FormDef deserializedFormDef = deserializeAndCleanUpSerializedForm(serializedForm);
        assertTrue(deserializedFormDef.getFormInstances().containsKey("external-xml"));
    }

    @Test
    public void itemsFromExternalSecondaryCSVInstance_ShouldBeAvailableToXPathParser() throws IOException, XPathSyntaxException {
        Path formName = r("external-select-csv.xml");
        setUpSimpleReferenceManager(formName.getParent(), "file-csv");
        FormDef formDef = parse(formName);
        assertEquals("CSV External Secondary Instance", formDef.getTitle());
        
        TreeReference treeReference = ((XPathPathExpr) parseXPath("instance('external-csv')/root/item")).getReference();
        EvaluationContext evaluationContext = formDef.getEvaluationContext();
        List<TreeReference> treeReferences = evaluationContext.expandReference(treeReference);
        assertThat(treeReferences.size(), is(12));

        AbstractTreeElement fifthItem = formDef.getNonMainInstance("external-csv").resolveReference(treeReferences.get(4));
        assertThat(fifthItem.getChild("label", 0).getValue().getDisplayText(), is("AB"));
    }

    // ODK Collect has CSV-parsing features that bypass XPath and use databases. This test verifies that if a
    // secondary instance is declared but not referenced in an instance() call, it is ignored by JavaRosa.
    @Test
    public void externalInstanceDeclaration_ShouldBeIgnored_WhenNotReferenced() {
        Path formPath = r("unused-secondary-instance.xml");
        setUpSimpleReferenceManager(formPath.getParent(), "file-csv");
        FormParseInit fpi = new FormParseInit(formPath);
        FormDef formDef = fpi.getFormDef();

        assertThat(formDef.getNonMainInstance("external-csv"), nullValue());
    }

    @Test
    public void externalInstanceDeclaration_ShouldBeIgnored_WhenNotReferenced_AfterParsingFormWithReference() {
        Path formPath = r("external-select-csv.xml");
        setUpSimpleReferenceManager(formPath.getParent(), "file-csv");
        FormParseInit fpi = new FormParseInit(formPath);
        FormDef formDef = fpi.getFormDef();
        assertThat(formDef.getNonMainInstance("external-csv").getRoot().hasChildren(), is(true));

        formPath = r("unused-secondary-instance.xml");
        fpi = new FormParseInit(formPath);
        formDef = fpi.getFormDef();
        assertThat(formDef.getNonMainInstance("external-csv"), nullValue());
    }

    // See https://github.com/getodk/javarosa/issues/451
    @Test
    public void dummyNodesInExternalInstanceDeclaration_ShouldBeIgnored() throws IOException, XPathSyntaxException {
        Path formPath = r("external-select-xml-dummy-nodes.xml");
        setUpSimpleReferenceManager(formPath.getParent(), "file");
        FormDef formDef = parse(formPath);

        TreeReference treeReference = ((XPathPathExpr) parseXPath("instance('external-xml')/root/item")).getReference();
        List<TreeReference> dataSet = formDef.getEvaluationContext().expandReference(treeReference);
        assertThat(dataSet.size(), is(12));
    }

    @Test
    public void emptyPlaceholderInstanceIsUsed_whenExternalInstanceNotFound() {
        // configure ReferenceManager on folder that doesn't exist
        setUpSimpleReferenceManager(r("external-select-csv.xml"), "file-csv");
        Scenario scenario = Scenario.init("external-select-csv.xml");

        assertThat(scenario.choicesOf("/data/first").size(), is(0));
    }

    @Test
    public void realInstanceIsResolved_whenFormIsDeserialized_afterPlaceholderInstanceUsed_andFileNowExists() throws IOException, DeserializationException {
        // configure ReferenceManager on folder that doesn't exist
        setUpSimpleReferenceManager(r("external-select-csv.xml"), "file-csv");
        Scenario scenario = Scenario.init("external-select-csv.xml");

        setUpSimpleReferenceManager(r("external-select-csv.xml").getParent(), "file-csv");
        scenario = scenario.serializeAndDeserializeForm();

        scenario.next();
        scenario.answer(scenario.choicesOf("/data/first").get(2));
        assertThat(((Selection) scenario.answerOf("/data/first").getValue()).getValue(), is("c"));
    }

    @Test
    // Clients would typically catch this exception and try parsing the form again which would succeed by using the placeholder.
    public void fileNotFoundException_whenFormIsDeserialized_afterPlaceholderInstanceUsed_andFileStillMissing() throws IOException, DeserializationException {
        // configure ReferenceManager on folder that doesn't exist
        setUpSimpleReferenceManager(r("external-select-csv.xml"), "file-csv");
        Scenario scenario = Scenario.init("external-select-csv.xml");

        try {
            scenario.serializeAndDeserializeForm();
            fail("Expected FileNotFoundException");
        } catch (FileNotFoundException e) {
            // pass
        }
    }

    @Test
    // It would be possible for a formdef to be serialized without access to the external secondary instance and then
    // deserialized with access. In that case, there's nothing to validate that the value and label references for a
    // dynamic select correspond to real nodes in the secondary instance so there's a runtime exception when making a choice.
    public void exceptionFromChoiceSelection_whenFormIsDeserialized_afterPlaceholderInstanceUsed_andFileMissingColumns() throws IOException, DeserializationException {
        // configure ReferenceManager on folder that doesn't exist
        setUpSimpleReferenceManager(r("external-select-csv.xml"), "file-csv");
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("first")
                    )),

                    t("instance id=\"external-csv\" src=\"jr://file-csv/external-data.csv\""),

                    bind("/data/first").type("string")
                )
            ),
            body(
                // Define a select using value and label references that don't exist in the secondary instance
                select1Dynamic("/data/first", "instance('external-csv')/root/item", "foo", "bar")
            )));

        setUpSimpleReferenceManager(r("external-select-csv.xml").getParent(), "file-csv");
        scenario = scenario.serializeAndDeserializeForm();

        scenario.next();
        try {
            scenario.answer(scenario.choicesOf("/data/first").get(0));
            fail("Expected runtime exception when making selection");
        } catch (RuntimeException e) {
            // pass
        }
    }
}
