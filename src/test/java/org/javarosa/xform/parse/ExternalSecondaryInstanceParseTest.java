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
    public void externalSecondaryInstanceFormSavesAndRestores() throws IOException, DeserializationException {
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
        TreeReference treeReference = ((XPathPathExpr)
            parseXPath("instance('towns')/data_set")).getReference();
        EvaluationContext evaluationContext = formDef.getEvaluationContext();
        List<TreeReference> treeReferences = evaluationContext.expandReference(treeReference);
        assertEquals(1, treeReferences.size());
        DataInstance townInstance = formDef.getNonMainInstance("towns");
        AbstractTreeElement tiRoot = townInstance.getRoot();
        AbstractTreeElement townData = tiRoot.getChild("towndata", 0);
        assertEquals(1, townData.getNumChildren());
        AbstractTreeElement dataSetChild = townData.getChild("data_set", 0);
        assertEquals("us_east", dataSetChild.getValue().getDisplayText());
    }

    @Test
    public void parsesExternalSecondaryInstanceForm2() throws IOException {
        Path formName = r("external_select_10.xml");
        mapFileToResourcePath(formName);
        FormDef formDef = parse(formName);
        assertEquals("external select 10", formDef.getTitle());
    }

    @Test
    public void parsesExternalSecondaryInstanceCsvForm() throws IOException {
        Path formName = r("external-select-csv.xml");
        mapFileToResourcePath(formName);
        FormDef formDef = parse(formName);
        assertEquals("external select 10", formDef.getTitle());
    }

    @Test
    public void externalInstanceDeclaration_ShouldBeIgnored_WhenNotReferenced() {
        Path formPath = r("unused-secondary-instance.xml");
        setUpSimpleReferenceManager("file-csv", formPath.getParent());
        FormParseInit fpi = new FormParseInit(formPath);
        FormDef formDef = fpi.getFormDef();

        assertThat(formDef.getNonMainInstance("fruits").getRoot().hasChildren(), is(false));
    }

    @Test
    public void parsesPreloadForm() throws IOException {
        // The form on this test uses a jr://file-csv resource.
        // We need to prime the ReferenceManager to deal with those
        Path form = r("Sample-Preloading.xml");
        setUpSimpleReferenceManager("file-csv", form.getParent());
        FormDef formDef = parse(form);
        assertEquals("Sample Form - Preloading", formDef.getTitle());
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
