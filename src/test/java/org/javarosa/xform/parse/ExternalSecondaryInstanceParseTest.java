package org.javarosa.xform.parse;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.instance.AbstractTreeElement;
import org.javarosa.core.model.instance.DataInstance;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.test.FormParseInit;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.xpath.expr.XPathPathExpr;
import org.javarosa.xpath.parser.XPathSyntaxException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.javarosa.core.reference.ReferenceManagerTestUtils.setUpSimpleReferenceManager;
import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.javarosa.xform.parse.FormParserHelper.mapFileToResourcePath;
import static org.javarosa.xform.parse.FormParserHelper.parse;
import static org.javarosa.xform.parse.FormParserHelper.serAndDeserializeForm;
import static org.javarosa.xform.parse.FormParserHelper.timeParsing;
import static org.javarosa.xpath.XPathParseTool.parseXPath;
import static org.junit.Assert.assertEquals;

public class ExternalSecondaryInstanceParseTest {
    private static final Logger logger = LoggerFactory.getLogger(ExternalSecondaryInstanceParseTest.class);

    private static Path EXTERNAL_SECONDARY_INSTANCE_XML;
    private static Path EXTERNAL_SECONDARY_INSTANCE_LARGE_XML;

    @BeforeClass
    public static void setUp() {
        EXTERNAL_SECONDARY_INSTANCE_XML = r("external-secondary-instance.xml");
        EXTERNAL_SECONDARY_INSTANCE_LARGE_XML = r("external-secondary-instance-large.xml");
    }

    @Test
    public void formWithExternalSecondaryXMLInstance_ShouldParseWithoutError() throws IOException, XPathSyntaxException {
        Path formName = r("external-select-xml.xml");
        mapFileToResourcePath(formName);
        FormDef formDef = parse(formName);
        assertEquals("XML External Secondary Instance", formDef.getTitle());

        // Confirm that items are made available to the XPath parser
        TreeReference treeReference = ((XPathPathExpr) parseXPath("instance('external-xml')/root/item")).getReference();
        EvaluationContext evaluationContext = formDef.getEvaluationContext();
        List<TreeReference> treeReferences = evaluationContext.expandReference(treeReference);
        assertThat(treeReferences.size(), is(12));
    }

    @Test
    public void formWithExternalSecondaryXMLInstance_ShouldSerializeAndDeserializeWithoutError() throws IOException, DeserializationException {
        Path formPath = EXTERNAL_SECONDARY_INSTANCE_XML;
        mapFileToResourcePath(formPath);
        serAndDeserializeForm(formPath);
    }


    @Test
    public void parsesExternalSecondaryInstanceForm() throws IOException, XPathSyntaxException {
        Path formName = EXTERNAL_SECONDARY_INSTANCE_XML;
        mapFileToResourcePath(formName);
        FormDef formDef = parse(formName);
        assertEquals("Form with external secondary instance", formDef.getTitle());

        TreeReference treeReference = ((XPathPathExpr) parseXPath("instance('towns')/root/towndata/data_set")).getReference();
        EvaluationContext evaluationContext = formDef.getEvaluationContext();
        List<TreeReference> treeReferences = evaluationContext.expandReference(treeReference);
        assertEquals(2, treeReferences.size());

        DataInstance townInstance = formDef.getNonMainInstance("towns");
        AbstractTreeElement tiRoot = townInstance.getRoot();
        AbstractTreeElement townData = tiRoot.getChild("towndata", 1);
        assertEquals(1, townData.getNumChildren());
        AbstractTreeElement dataSetChild = townData.getChild("data_set", 0);
        assertEquals("us_west", dataSetChild.getValue().getDisplayText());
    }

    @Test
    public void itemsFromExternalSecondaryCSVInstance_ShouldBeAvailableToXPathParser() throws IOException, XPathSyntaxException {
        Path formName = r("external-select-csv.xml");
        mapFileToResourcePath(formName);
        FormDef formDef = parse(formName);
        assertEquals("CSV External Secondary Instance", formDef.getTitle());
        
        TreeReference treeReference = ((XPathPathExpr) parseXPath("instance('external-csv')/root/item")).getReference();
        EvaluationContext evaluationContext = formDef.getEvaluationContext();
        List<TreeReference> treeReferences = evaluationContext.expandReference(treeReference);
        assertThat(treeReferences.size(), is(12));
    }

    // ODK Collect has CSV-parsing features that bypass XPath and use databases. This test verifies that if a
    // secondary instance is declared but not referenced in an instance() call, it is ignored by JavaRosa.
    @Test
    public void externalInstanceDeclaration_ShouldBeIgnored_WhenNotReferenced() {
        Path formPath = r("unused-secondary-instance.xml");
        setUpSimpleReferenceManager("file-csv", formPath.getParent());
        FormParseInit fpi = new FormParseInit(formPath);
        FormDef formDef = fpi.getFormDef();

        assertThat(formDef.getNonMainInstance("external-csv"), nullValue());
    }

    @Test
    public void externalInstanceDeclaration_ShouldBeIgnored_WhenNotReferenced_AfterParsingFormWithReference() {
        Path formPath = r("external-select-csv.xml");
        setUpSimpleReferenceManager("file-csv", formPath.getParent());
        FormParseInit fpi = new FormParseInit(formPath);
        FormDef formDef = fpi.getFormDef();
        assertThat(formDef.getNonMainInstance("external-csv").getRoot().hasChildren(), is(true));

        formPath = r("unused-secondary-instance.xml");
        fpi = new FormParseInit(formPath);
        formDef = fpi.getFormDef();
        assertThat(formDef.getNonMainInstance("external-csv"), nullValue());
    }

    @Test
    public void timesParsingLargeExternalSecondaryInstanceFiles() throws IOException {
        Path tempDir = Files.createTempDirectory("javarosa-test-");
        Path tempFile = tempDir.resolve("towns-large.xml");
        timeParsing(new LargeEsiFileGenerator(), tempFile, EXTERNAL_SECONDARY_INSTANCE_LARGE_XML, logger);
    }

    /**
     * Generates large versions of an external secondary instance, from scratch
     */
    class LargeEsiFileGenerator implements FormParserHelper.LargeInstanceFileGenerator {
        @Override
        public void createLargeInstanceSource(Path outputFilename, int numChildren) throws IOException {
            PrintWriter pw = new PrintWriter(outputFilename.toString());
            pw.println("<towndata>");
            for (int i = 0; i < numChildren; ++i) {
                pw.println("<data_set>us_east</data_set>");
            }
            pw.println("</towndata>");
            pw.close();
        }
    }
}
