package org.javarosa.xform.parse;

import static java.nio.file.Files.copy;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.javarosa.core.model.Constants.CONTROL_RANGE;
import static org.javarosa.core.model.Constants.CONTROL_RANK;
import static org.javarosa.core.test.AnswerDataMatchers.intAnswer;
import static org.javarosa.core.util.BindBuilderXFormsElement.bind;
import static org.javarosa.core.util.XFormsElement.body;
import static org.javarosa.core.util.XFormsElement.head;
import static org.javarosa.core.util.XFormsElement.html;
import static org.javarosa.core.util.XFormsElement.input;
import static org.javarosa.core.util.XFormsElement.mainInstance;
import static org.javarosa.core.util.XFormsElement.model;
import static org.javarosa.core.util.XFormsElement.t;
import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.javarosa.xform.parse.FormParserHelper.deserializeAndCleanUpSerializedForm;
import static org.javarosa.xform.parse.FormParserHelper.getSerializedFormPath;
import static org.javarosa.xform.parse.FormParserHelper.parse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.RangeQuestion;
import org.javarosa.core.model.SubmissionProfile;
import org.javarosa.core.model.actions.Actions;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.AbstractTreeElement;
import org.javarosa.core.model.instance.DataInstance;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.InstanceInitializationFactory;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.reference.ReferenceManagerTestUtils;
import org.javarosa.core.services.transport.payload.ByteArrayPayload;
import org.javarosa.core.test.Scenario;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.model.xform.XFormSerializingVisitor;
import org.javarosa.model.xform.XPathReference;
import org.javarosa.xpath.parser.XPathSyntaxException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kxml2.kdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XFormParserTest {
    private static final Logger logger = LoggerFactory.getLogger(XFormParserTest.class);

    private static Path FORM_INSTANCE_XML_FILE_NAME;
    private static Path SECONDARY_INSTANCE_XML;
    private static Path SECONDARY_INSTANCE_LARGE_XML;

    private static final String AUDIT_NODE = "audit";
    private static final String AUDIT_ANSWER = "audit111.csv";

    private static final String AUDIT_2_NODE = "audit2";
    private static final String AUDIT_2_ANSWER = "audit222.csv";

    private static final String AUDIT_3_NODE = "audit3";
    private static final String AUDIT_3_ANSWER = "audit333.csv";

    private static final String ORX_2_NAMESPACE_PREFIX = "orx2";
    private static final String ORX_2_NAMESPACE_URI = "http://openrosa.org/xforms";

    @Before
    public void setUp() {
        try {
            FORM_INSTANCE_XML_FILE_NAME = Files.createTempFile("instance.xml", null);
            SECONDARY_INSTANCE_XML = r("secondary-instance.xml");
            SECONDARY_INSTANCE_LARGE_XML = Files.createTempFile("secondary-instance-large.xml", null);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @After
    public void tearDown() throws Exception {
        Files.deleteIfExists(FORM_INSTANCE_XML_FILE_NAME);
    }

    @Test
    public void parsesSimpleForm() throws IOException {
        FormDef formDef = parse(r("simple-form.xml"));
        assertEquals(formDef.getTitle(), "Simple Form");
    }

    @Test
    public void parsesForm2() throws IOException {
        FormDef formDef = parse(r("form2.xml"));
        assertEquals("My Survey", formDef.getTitle());
        assertEquals(3, formDef.getChildren().size());
        assertEquals("What is your first name?", formDef.getChild(0).getLabelInnerText());
    }
    
    @Test
    public void spacesBetweenOutputs_areRespected() throws IOException {
        Scenario scenario = Scenario.init("spaces-outputs", html(
            head(
                model(
                    mainInstance(t("data id=\"spaces-outputs\"",
                        t("first_name"),
                        t("last_name"),
                        t("question")
                    )),
                    bind("/data/question").type("string")
                )),
            body(
                input("/data/question",
                        t("label", "Full name: <output value=\" ../first_name \"/>\u00A0<output value=\" ../last_name \"/>"))
            )
        ));

        scenario.next();
        String innerText = scenario.getQuestionAtIndex().getLabelInnerText();
        char nbsp = 0x00A0;
        String expected = "Full name: ${0}" + nbsp + "${1}";
        assertEquals(expected, innerText);
    }
 
    @Test
    public void sumWorksWithStrings() throws IOException {
        
        Scenario scenario = Scenario.init("sum_test.xml");

        scenario.next();
        scenario.getQuestionAtIndex().getLabelInnerText();
        scenario.answer("35.1189");
        scenario.next();
        scenario.answer(3);
        scenario.next();
        scenario.next();
        scenario.answer("20.877");
        scenario.next();
        scenario.next();
        scenario.answer("2.7859");
        assertFalse((Boolean)scenario.getAnswerNode("/data/valid").getValue().getValue());
        scenario.next();
        scenario.next();
        scenario.answer("11.456");
        assertTrue((Boolean)scenario.getAnswerNode("/data/valid").getValue().getValue());


    }
    
    @Test
    public void parsesSecondaryInstanceForm() throws IOException {
        FormDef formDef = parse(SECONDARY_INSTANCE_XML);
        assertEquals("Form with secondary instance", formDef.getTitle());
    }

    @Test
    public void parsesSecondaryInstanceForm2() throws IOException {
        Path formName = r("internal_select_10.xml");
        FormDef formDef = parse(formName);
        assertEquals("internal select 10", formDef.getTitle());
    }

    @Test
    public void parsesLastSavedInstanceWithNullSrc() throws IOException {
        Path formName = r("last-saved-blank.xml");
        FormDef formDef = parse(formName, null);
        assertEquals("Form with last-saved instance (blank)", formDef.getTitle());

        DataInstance lastSaved = formDef.getNonMainInstance("last-saved");
        AbstractTreeElement root = lastSaved.getRoot();
        assertEquals(0, root.getNumChildren());
    }

    @Test
    public void parsesLastSavedInstanceWithFilledForm() throws IOException {
        Path formName = r("last-saved-blank.xml");
        Path lastSavedSubmissionDirectory = r("last-saved-filled.xml").toAbsolutePath().getParent();
        ReferenceManagerTestUtils.setUpSimpleReferenceManager(lastSavedSubmissionDirectory, "file");
        FormDef formDef = parse(formName, "jr://file/last-saved-filled.xml");
        assertEquals("Form with last-saved instance (blank)", formDef.getTitle());

        DataInstance lastSaved = formDef.getNonMainInstance("last-saved");
        AbstractTreeElement root = lastSaved.getRoot();
        AbstractTreeElement item = root
            .getChild("head", 0)
            .getChild("model", 0)
            .getChild("instance", 0)
            .getChild("data", 0)
            .getChild("item", 0);
        assertEquals("Foo", item.getValue().getDisplayText());
    }

    @Test
    public void multipleInstancesFormSavesAndRestores() throws IOException, DeserializationException {
        FormDef originalFormDef = parse(r("Simpler_Cascading_Select_Form.xml"));

        Path serializedForm = getSerializedFormPath(originalFormDef);
        FormDef deserializedFormDef = deserializeAndCleanUpSerializedForm(serializedForm);

        assertThat(originalFormDef.getTitle(), is(deserializedFormDef.getTitle()));
    }

    /**
     * ensure serializing and deserializing a range form is done without errors
     * see https://github.com/getodk/javarosa/issues/245 why this is needed
     */
    @Test
    public void rangeFormSavesAndRestores() throws IOException, DeserializationException {
        FormDef originalFormDef = parse(r("range-form.xml"));

        Path serializedForm = getSerializedFormPath(originalFormDef);
        FormDef deserializedFormDef = deserializeAndCleanUpSerializedForm(serializedForm);

        assertThat(originalFormDef.getTitle(), is(deserializedFormDef.getTitle()));
    }

    @Test
    public void parsesRankForm() throws IOException {
        FormDef formDef = parse(r("rank-form.xml"));
        assertEquals(formDef.getTitle(), "Rank Form");
        assertEquals(1, formDef.getChildren().size());
        assertEquals(CONTROL_RANK, ((QuestionDef) formDef.getChild(0)).getControlType());
        assertNoParseErrors(formDef);
    }

    @Test
    public void parsesRangeForm() throws IOException {
        FormDef formDef = parse(r("range-form.xml"));
        RangeQuestion question = (RangeQuestion) formDef.getChild(0);
        assertEquals(CONTROL_RANGE, question.getControlType());
        assertEquals(-2.0d, question.getRangeStart().doubleValue(), 0);
        assertEquals(2.0d, question.getRangeEnd().doubleValue(), 0);
        assertEquals(0.5d, question.getRangeStep().doubleValue(), 0);
    }

    @Test(expected = XFormParseException.class)
    public void throwsParseExceptionOnBadRangeForm() throws IOException {
        parse(r("bad-range-form.xml"));
    }
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void throwsExceptionOnEmptySelect() throws IOException {
        exceptionRule.expect(XFormParseException.class);
        exceptionRule.expectMessage("Select question 'First' has no choices");

        Path formName = r("internal_empty_select.xml");
        FormDef formDef = parse(formName);
    }

    @Test
    public void formWithCountNonEmptyFunc_ShouldNotThrowException() throws IOException {
        Scenario scenario = Scenario.init("countNonEmptyForm.xml");
        assertThat(scenario.answerOf("/test/count_value"), is(intAnswer(4)));
        assertThat(scenario.answerOf("/test/count_non_empty_value"), is(intAnswer(2)));
    }

    @Test
    public void parsesMetaNamespaceForm() throws IOException {
        FormDef formDef = parse(r("meta-namespace-form.xml"));
        assertEquals(formDef.getTitle(), "Namespace for Metadata");
        assertNoParseErrors(formDef);
    }

    @Test
    public void serializeAndRestoreMetaNamespaceFormInstance() throws IOException {
        // Given
        FormDef formDef = parse(r("meta-namespace-form.xml"));
        assertEquals(formDef.getTitle(), "Namespace for Metadata");
        assertNoParseErrors(formDef);

        TreeElement audit = findDepthFirst(formDef.getInstance().getRoot(), AUDIT_NODE);
        TreeElement audit2 = findDepthFirst(formDef.getInstance().getRoot(), AUDIT_2_NODE);
        TreeElement audit3 = findDepthFirst(formDef.getInstance().getRoot(), AUDIT_3_NODE);

        assertNotNull(audit);
        assertEquals(ORX_2_NAMESPACE_PREFIX, audit.getNamespacePrefix());
        assertEquals(ORX_2_NAMESPACE_URI, audit.getNamespace());

        assertNotNull(audit2);
        assertEquals(ORX_2_NAMESPACE_PREFIX, audit2.getNamespacePrefix());
        assertEquals(ORX_2_NAMESPACE_URI, audit2.getNamespace());

        assertNotNull(audit3);
        assertNull(audit3.getNamespacePrefix());
        assertNull(audit3.getNamespace());

        audit.setAnswer(new StringData(AUDIT_ANSWER));
        audit2.setAnswer(new StringData(AUDIT_2_ANSWER));
        audit3.setAnswer(new StringData(AUDIT_3_ANSWER));

        // When

        // serialize the form instance
        XFormSerializingVisitor serializer = new XFormSerializingVisitor();
        ByteArrayPayload xml = (ByteArrayPayload) serializer.createSerializedPayload(formDef.getInstance());
        copy(xml.getPayloadStream(), FORM_INSTANCE_XML_FILE_NAME, REPLACE_EXISTING);

        // restore (deserialize) the form instance
        byte[] formInstanceBytes = readAllBytes(FORM_INSTANCE_XML_FILE_NAME);
        FormInstance formInstance = XFormParser.restoreDataModel(formInstanceBytes, null);

        // Then
        audit = findDepthFirst(formInstance.getRoot(), AUDIT_NODE);
        audit2 = findDepthFirst(formInstance.getRoot(), AUDIT_2_NODE);
        audit3 = findDepthFirst(formInstance.getRoot(), AUDIT_3_NODE);

        assertNotNull(audit);
        assertEquals(ORX_2_NAMESPACE_PREFIX, audit.getNamespacePrefix());
        assertEquals(ORX_2_NAMESPACE_URI, audit.getNamespace());
        assertEquals(AUDIT_ANSWER, audit.getValue().getValue());

        assertNotNull(audit2);
        assertEquals(ORX_2_NAMESPACE_PREFIX, audit2.getNamespacePrefix());
        assertEquals(ORX_2_NAMESPACE_URI, audit2.getNamespace());
        assertEquals(AUDIT_2_ANSWER, audit2.getValue().getValue());

        assertNotNull(audit3);
        assertNull(audit3.getNamespacePrefix());
        assertNull(audit3.getNamespace());
        assertEquals(AUDIT_3_ANSWER, audit3.getValue().getValue());
    }

    @Test
    public void parseFormWithTemplateRepeat() throws IOException {
        // Given & When
        FormDef formDef = parse(r("template-repeat.xml"));

        // Then
        assertEquals(formDef.getTitle(), "Repeat with template");
        assertNoParseErrors(formDef);
    }

    @Test
    public void parseIMCIbyDTreeForm() throws IOException {
        // Given & When
        FormDef formDef = parse(r("eIMCI-by-D-Tree.xml"));

        // Then
        assertEquals(formDef.getTitle(), "eIMCI by D-Tree");
        assertNoParseErrors(formDef);
    }

    @Test
    public void parseFormWithSubmissionElement() throws IOException {
        // Given & When
        FormDef formDef = parse(r("submission-element.xml"));

        // Then
        assertEquals(formDef.getTitle(), "Single Submission Element");
        assertNoParseErrors(formDef);

        SubmissionProfile submissionProfile = formDef.getSubmissionProfile();
        assertEquals("http://some.destination.com", submissionProfile.getAction());
        assertEquals("form-data-post", submissionProfile.getMethod());
        assertNull(submissionProfile.getMediaType());
        assertEquals("/data/text", submissionProfile.getRef().getReference().toString());
    }

    /**
     * Simple tests that documents assumption that the model has to come before the body tag.
     * According to the comment above {@link XFormParser#parseModel(Element)} method,
     * this is not mandated by the specs but has been implemented this way to keep parsing simpler.
     */
    @Test(expected = RuntimeException.class)
    public void parseFormWithBodyBeforeModel() throws IOException {
        parse(r("body-before-model.xml"));
    }

    @Test
    public void parseFormWithTwoModels() throws IOException {
        // Given & When
        FormDef formDef = parse(r("two-models.xml"));

        // Then
        assertEquals(formDef.getTitle(), "Two Models");
        List<String> parseWarnings = formDef.getParseWarnings();
        assertEquals("Number of error messages", 1, parseWarnings.size());
        assertEquals("XForm Parse Warning: Multiple models not supported. Ignoring subsequent models.\n" +
            "    Problem found at nodeset: /html/head/model\n" +
            "    With element <model><instance><data id=\"second-model\">...\n" +
            "", parseWarnings.get(0));
        String firstModelInstanceId =
            (String) formDef
                .getMainInstance()
                .getRoot()
                .getAttribute(null, "id")
                .getValue()
                .getValue();
        assertEquals("first-model", firstModelInstanceId);
    }

    @Test
    public void parseFormWithSetValueAction() throws IOException {
        // Given & When
        FormDef formDef = parse(r("form-with-setvalue-action.xml"));

        // dispatch 'odk-instance-first-load' event (Actions.EVENT_ODK_INSTANCE_FIRST_LOAD)
        formDef.initialize(true, new InstanceInitializationFactory());

        // Then
        assertEquals(formDef.getTitle(), "SetValue action");
        assertNoParseErrors(formDef);
        assertEquals(1, formDef.getActionController().getListenersForEvent(Actions.EVENT_ODK_INSTANCE_FIRST_LOAD).size());

        TreeElement textNode =
            formDef.getMainInstance().getRoot().getChildrenWithName("text").get(0);

        assertEquals("Test Value", textNode.getValue().getValue());
    }

    @Test
    public void parseGroupWithNodesetAttrForm() throws IOException {
        // Given & When
        FormDef formDef = parse(r("group-with-nodeset-attr.xml"));

        // Then
        assertEquals(formDef.getTitle(), "group with nodeset attribute");
        assertEquals("Number of error messages", 0, formDef.getParseErrors().size());

        final TreeReference expectedTreeReference = new TreeReference();
        expectedTreeReference.setRefLevel(-1); // absolute reference
        expectedTreeReference.add("data", -1); // the instance root
        expectedTreeReference.add("R1", -1); // the outer repeat
        expectedTreeReference.add("G2", -1); // the inner group
        final IDataReference expectedXPathReference = new XPathReference(expectedTreeReference);

        IFormElement groupElement = formDef.getChild(0).getChild(0);

        assertThat(groupElement, instanceOf(GroupDef.class));
        assertThat(((GroupDef) groupElement).getRepeat(), is(false));
        assertThat(groupElement.getBind(), is(expectedXPathReference));
    }

    @Test
    public void parseGroupWithRefAttrForm() throws IOException, XPathSyntaxException {
        // Given & When
        FormDef formDef = parse(r("group-with-ref-attr.xml"));

        // Then
        assertEquals(formDef.getTitle(), "group with ref attribute");
        assertEquals("Number of error messages", 0, formDef.getParseErrors().size());

        final TreeReference g2TreeRef = new TreeReference();
        g2TreeRef.setRefLevel(-1); // absolute reference
        g2TreeRef.add("data", -1); // the instance root
        g2TreeRef.add("G1", -1); // the outer group
        g2TreeRef.add("G2", -1); // the inner group

        // G2 does NOT have a `ref`.
        // Collect implicitly assumes the TreeReference will be created like this.
        IDataReference g2AbsRef = FormDef.getAbsRef(null, g2TreeRef.getParentRef());

        IFormElement g2Element = formDef.getChild(0).getChild(0);
        assertThat(g2Element.getBind(), is(g2AbsRef));

        final TreeReference g3TreeRef = new TreeReference();
        g3TreeRef.setRefLevel(-1); // absolute reference
        g3TreeRef.add("data", -1); // the instance root
        g3TreeRef.add("G1", -1); // the outer group
        g3TreeRef.add("G3", -1); // the inner group

        // G3 has a `ref`.
        // Collect implicitly assumes the TreeReference will be created like this.
        IDataReference g3AbsRef = FormDef.getAbsRef(new XPathReference(g3TreeRef), g3TreeRef.getParentRef());

        IFormElement g3Element = formDef.getChild(0).getChild(1);
        assertThat(g3Element.getBind(), is(g3AbsRef));
    }

    @Test
    public void testSetValueWithStrings() throws IOException {
        
        Scenario scenario = Scenario.init("default_test.xml");
        assertEquals("string-value", scenario.getAnswerNode("/data/string_val").getValue().getValue().toString());
        assertEquals("inline-value", scenario.getAnswerNode("/data/inline_val").getValue().getValue().toString());
    }
    
    private TreeElement findDepthFirst(TreeElement parent, String name) {
        int len = parent.getNumChildren();
        for (int i = 0; i < len; ++i) {
            TreeElement e = parent.getChildAt(i);
            if (name.equals(e.getName())) {
                return e;
            } else if (e.getNumChildren() != 0) {
                TreeElement v = findDepthFirst(e, name);
                if (v != null) {
                    return v;
                }
            }
        }
        return null;
    }

    private void assertNoParseErrors(FormDef formDef) {
        assertEquals("Number of error messages", 0, formDef.getParseErrors().size());
    }
}
