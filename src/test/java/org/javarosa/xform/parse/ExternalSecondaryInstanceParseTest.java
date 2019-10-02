package org.javarosa.xform.parse;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.instance.AbstractTreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.test.FormParseInit;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.xpath.expr.XPathPathExpr;
import org.javarosa.xpath.parser.XPathSyntaxException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.javarosa.core.reference.ReferenceManagerTestUtils.setUpSimpleReferenceManager;
import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.javarosa.xform.parse.FormParserHelper.deserializeAndCleanUpSerializedForm;
import static org.javarosa.xform.parse.FormParserHelper.getSerializedFormPath;
import static org.javarosa.xform.parse.FormParserHelper.parse;
import static org.javarosa.xpath.XPathParseTool.parseXPath;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExternalSecondaryInstanceParseTest {
    private static final Logger logger = LoggerFactory.getLogger(ExternalSecondaryInstanceParseTest.class);

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

    // See https://github.com/opendatakit/javarosa/issues/451
    @Test
    public void dummyNodesInExternalInstanceDeclaration_ShouldBeIgnored() throws IOException, XPathSyntaxException {
        Path formPath = r("external-select-xml-dummy-nodes.xml");
        setUpSimpleReferenceManager(formPath.getParent(), "file");
        FormDef formDef = parse(formPath);

        TreeReference treeReference = ((XPathPathExpr) parseXPath("instance('external-xml')/root/item")).getReference();
        List<TreeReference> dataSet = formDef.getEvaluationContext().expandReference(treeReference);
        assertThat(dataSet.size(), is(12));
    }
}
