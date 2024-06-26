package org.javarosa.xform.parse;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.model.instance.AbstractTreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.test.FormParseInit;
import org.javarosa.test.Scenario;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.xpath.expr.XPathPathExpr;
import org.javarosa.xpath.parser.XPathSyntaxException;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.javarosa.core.reference.ReferenceManagerTestUtils.setUpSimpleReferenceManager;
import static org.javarosa.test.BindBuilderXFormsElement.bind;
import static org.javarosa.test.XFormsElement.body;
import static org.javarosa.test.XFormsElement.head;
import static org.javarosa.test.XFormsElement.html;
import static org.javarosa.test.XFormsElement.mainInstance;
import static org.javarosa.test.XFormsElement.model;
import static org.javarosa.test.XFormsElement.select1Dynamic;
import static org.javarosa.test.XFormsElement.t;
import static org.javarosa.test.XFormsElement.title;
import static org.javarosa.test.ResourcePathHelper.r;
import static org.javarosa.xform.parse.FormParserHelper.deserializeAndCleanUpSerializedForm;
import static org.javarosa.xform.parse.FormParserHelper.getSerializedFormPath;
import static org.javarosa.xform.parse.FormParserHelper.parse;
import static org.javarosa.xpath.XPathParseTool.parseXPath;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ExternalSecondaryInstanceParseTest {

    //region Parsing of different file types into external secondary instances
    @Test
    public void itemsFromExternalSecondaryXMLInstance_ShouldBeAvailableToXPathParser() throws IOException, XPathSyntaxException, XFormParser.ParseException {
        configureReferenceManagerCorrectly();

        FormDef formDef = parse(r("external-select-xml.xml"));
        assertEquals("XML External Secondary Instance", formDef.getTitle());

        TreeReference treeReference = ((XPathPathExpr) parseXPath("instance('external-xml')/root/item")).getReference();
        EvaluationContext evaluationContext = formDef.getEvaluationContext();
        List<TreeReference> treeReferences = evaluationContext.expandReference(treeReference);
        assertThat(treeReferences.size(), is(12));

        AbstractTreeElement fifthItem = formDef.getNonMainInstance("external-xml").resolveReference(treeReferences.get(4));
        assertThat(fifthItem.getChild("label", 0).getValue().getDisplayText(), is("AB"));
    }

    @Test
    public void itemsFromExternalSecondaryGeoJsonInstance_ShouldBeAvailableToXPathParser() throws IOException, XPathSyntaxException, XFormParser.ParseException {
        configureReferenceManagerCorrectly();

        FormDef formDef = parse(r("external-select-geojson.xml"));
        assertEquals("GeoJSON External Secondary Instance", formDef.getTitle());

        TreeReference treeReference = ((XPathPathExpr) parseXPath("instance('external-geojson')/root/item")).getReference();
        EvaluationContext evaluationContext = formDef.getEvaluationContext();
        List<TreeReference> treeReferences = evaluationContext.expandReference(treeReference);
        assertThat(treeReferences.size(), is(2));

        AbstractTreeElement secondItem = formDef.getNonMainInstance("external-geojson").resolveReference(treeReferences.get(1));
        assertThat(secondItem.getChild("name", 0).getValue().getDisplayText(), is("Your cool point"));
    }

    @Test
    public void itemsFromExternalSecondaryGeoJsonInstance_withIntegerIds_canBeSelected() throws XFormParser.ParseException {
        configureReferenceManagerCorrectly();

        Scenario scenario = Scenario.init(r("external-select-geojson.xml"));
        SelectChoice choiceWithIntId = scenario.choicesOf("/data/q").get(1);
        scenario.next();
        scenario.answer(choiceWithIntId);
        assertThat(scenario.answerOf("/data/q").getDisplayText(), is("67"));
    }

    @Test
    public void itemsFromExternalSecondaryCSVInstance_ShouldBeAvailableToXPathParser() throws IOException, XPathSyntaxException, XFormParser.ParseException {
        configureReferenceManagerCorrectly();

        FormDef formDef = parse(r("external-select-csv.xml"));
        assertEquals("CSV External Secondary Instance", formDef.getTitle());

        TreeReference treeReference = ((XPathPathExpr) parseXPath("instance('external-csv')/root/item")).getReference();
        EvaluationContext evaluationContext = formDef.getEvaluationContext();
        List<TreeReference> treeReferences = evaluationContext.expandReference(treeReference);
        assertThat(treeReferences.size(), is(12));

        AbstractTreeElement fifthItem = formDef.getNonMainInstance("external-csv").resolveReference(treeReferences.get(4));
        assertThat(fifthItem.getChild("label", 0).getValue().getDisplayText(), is("AB"));
    }
    //endregion

    @Test
    public void xformParseException_whenItemsetConfiguresValueOrLabelNotInExternalInstance() throws IOException {
        configureReferenceManagerCorrectly();

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
        } catch (XFormParser.ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void csvSecondaryInstanceWithHeaderOnly_parsesWithoutError() throws IOException, XFormParser.ParseException {
        configureReferenceManagerCorrectly();

        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("first")
                    )),

                    t("instance id=\"external-csv\" src=\"jr://file-csv/header_only.csv\""),

                    bind("/data/first").type("string")
                )
            ),
            body(
                select1Dynamic("/data/first", "instance('external-csv')/root/item")
            )));

        assertThat(scenario.choicesOf("/data/first").size(), is(0));
    }

    @Test
    public void formWithExternalSecondaryXMLInstance_ShouldSerializeAndDeserialize() throws IOException, DeserializationException, XFormParser.ParseException {
        configureReferenceManagerCorrectly();

        FormDef originalFormDef = parse(r("external-select-xml.xml"));

        Path serializedForm = getSerializedFormPath(originalFormDef);
        FormDef deserializedFormDef = deserializeAndCleanUpSerializedForm(serializedForm);

        assertThat(originalFormDef.getTitle(), is(deserializedFormDef.getTitle()));
    }

    @Test
    public void deserializedFormDefCreatedFromAFormWithExternalSecondaryXMLInstance_ShouldContainThatExternalInstance() throws IOException, DeserializationException, XFormParser.ParseException {
        configureReferenceManagerCorrectly();

        Path formPath = r("external-select-xml.xml");
        FormDef originalFormDef = parse(r("external-select-xml.xml"));
        originalFormDef.setFormXmlPath(formPath.toString());

        Path serializedForm = getSerializedFormPath(originalFormDef);
        FormDef deserializedFormDef = deserializeAndCleanUpSerializedForm(serializedForm);
        assertTrue(deserializedFormDef.getFormInstances().containsKey("external-xml"));
    }

    //region ODK Collect database-driven external file features
    // ODK Collect has CSV-parsing features that bypass XPath and use databases. This test verifies that if a
    // secondary instance is declared but not referenced in an instance() call, it is ignored by JavaRosa.
    @Test
    public void externalInstanceDeclaration_ShouldBeIgnored_WhenNotReferenced() throws XFormParser.ParseException {
        configureReferenceManagerCorrectly();

        FormParseInit fpi = new FormParseInit(r("unused-secondary-instance.xml"));
        FormDef formDef = fpi.getFormDef();

        assertThat(formDef.getNonMainInstance("external-csv"), nullValue());
    }

    @Test
    public void externalInstanceDeclaration_ShouldBeIgnored_WhenNotReferenced_AfterParsingFormWithReference() throws XFormParser.ParseException {
        configureReferenceManagerCorrectly();

        FormParseInit fpi = new FormParseInit(r("external-select-csv.xml"));
        FormDef formDef = fpi.getFormDef();
        assertThat(formDef.getNonMainInstance("external-csv").getRoot().hasChildren(), is(true));

        fpi = new FormParseInit(r("unused-secondary-instance.xml"));
        formDef = fpi.getFormDef();
        assertThat(formDef.getNonMainInstance("external-csv"), nullValue());
    }
    //endregion

    // See https://github.com/getodk/javarosa/issues/451
    @Test
    public void dummyNodesInExternalInstanceDeclaration_ShouldBeIgnored() throws IOException, XPathSyntaxException, XFormParser.ParseException {
        configureReferenceManagerCorrectly();

        FormDef formDef = parse(r("external-select-xml-dummy-nodes.xml"));

        TreeReference treeReference = ((XPathPathExpr) parseXPath("instance('external-xml')/root/item")).getReference();
        List<TreeReference> dataSet = formDef.getEvaluationContext().expandReference(treeReference);
        assertThat(dataSet.size(), is(12));
    }

    //region Missing external file
    @Test
    public void emptyPlaceholderInstanceIsUsed_whenExternalInstanceNotFound() throws XFormParser.ParseException {
        configureReferenceManagerIncorrectly();
        Scenario scenario = Scenario.init("external-select-csv.xml");

        assertThat(scenario.choicesOf("/data/first").size(), is(0));
    }

    @Test
    public void realInstanceIsResolved_whenFormIsDeserialized_afterPlaceholderInstanceUsed_andFileNowExists() throws IOException, DeserializationException, XFormParser.ParseException {
        configureReferenceManagerIncorrectly();
        Scenario scenario = Scenario.init("external-select-csv.xml");

        configureReferenceManagerCorrectly();
        scenario = scenario.serializeAndDeserializeForm();

        scenario.next();
        scenario.answer(scenario.choicesOf("/data/first").get(2));
        assertThat(((Selection) scenario.answerOf("/data/first").getValue()).getValue(), is("c"));
    }

    @Test
    // Clients would typically catch this exception and try parsing the form again which would succeed by using the placeholder.
    public void fileNotFoundException_whenFormIsDeserialized_afterPlaceholderInstanceUsed_andFileStillMissing() throws IOException, DeserializationException, XFormParser.ParseException {
        configureReferenceManagerIncorrectly();
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
    public void exceptionFromChoiceSelection_whenFormIsDeserialized_afterPlaceholderInstanceUsed_andFileMissingColumns() throws IOException, DeserializationException, XFormParser.ParseException {
        configureReferenceManagerIncorrectly();

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

        configureReferenceManagerCorrectly();
        scenario = scenario.serializeAndDeserializeForm();

        scenario.next();
        try {
            scenario.answer(scenario.choicesOf("/data/first").get(0));
            fail("Expected runtime exception when making selection");
        } catch (RuntimeException e) {
            // pass
        }
    }
    //endregion

    // All external secondary instances and forms are in the same folder. Configure the ReferenceManager to resolve
    // URIs to that folder.
    public static void configureReferenceManagerCorrectly() {
        setUpSimpleReferenceManager(r("external-select-csv.xml").getParent(), "file-csv", "file");
    }

    // Configure the ReferenceManager to resolve URIs to a folder that does not exist.
    public static void configureReferenceManagerIncorrectly() {
        setUpSimpleReferenceManager(r("external-select-csv.xml"), "file-csv", "file");
    }
}
