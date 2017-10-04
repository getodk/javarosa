package org.javarosa.xform.parse;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.RangeQuestion;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.services.PrototypeManager;
import org.javarosa.core.services.transport.payload.ByteArrayPayload;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.model.xform.XFormSerializingVisitor;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.instance.AbstractTreeElement;
import org.javarosa.core.model.instance.DataInstance;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.model.xform.XFormsModule;
import org.javarosa.xpath.expr.XPathPathExpr;
import org.javarosa.xpath.parser.XPathSyntaxException;
import org.junit.After;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.Files.copy;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.javarosa.core.model.Constants.CONTROL_RANGE;
import static org.javarosa.core.util.externalizable.ExtUtil.defaultPrototypes;
import static org.javarosa.xpath.XPathParseTool.parseXPath;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class XFormParserTest {

    /** Makes a Path for a resource file */
    private static Path r(String filename) {
        return Paths.get("resources", filename);
    }
    private static final Path FORM_INSTANCE_XML_FILE_NAME           = r("instance.xml");
    private static final Path SECONDARY_INSTANCE_XML                = r("secondary-instance.xml");
    private static final Path SECONDARY_INSTANCE_LARGE_XML          = r("secondary-instance-large.xml");
    private static final Path EXTERNAL_SECONDARY_INSTANCE_XML       = r("external-secondary-instance.xml");
    private static final Path EXTERNAL_SECONDARY_INSTANCE_LARGE_XML = r("external-secondary-instance-large.xml");

    private static final String AUDIT_NODE = "audit";
    private static final String AUDIT_ANSWER = "audit111.csv";

    private static final String AUDIT_2_NODE = "audit2";
    private static final String AUDIT_2_ANSWER = "audit222.csv";

    private static final String AUDIT_3_NODE = "audit3";
    private static final String AUDIT_3_ANSWER = "audit333.csv";

    private static final String ORX_2_NAMESPACE_PREFIX = "orx2";
    private static final String ORX_2_NAMESPACE_URI = "http://openrosa.org/xforms";

    @After
    public void tearDown() throws Exception {
        Files.deleteIfExists(FORM_INSTANCE_XML_FILE_NAME);
    }

    @Test
    public void parsesSimpleForm() throws IOException {
        FormDef formDef = parse(r("simple-form.xml")).formDef;
        assertEquals(formDef.getTitle(), "Simple Form");
    }

    @Test
    public void parsesForm2() throws IOException {
        FormDef formDef = parse(r("form2.xml")).formDef;
        assertEquals("My Survey", formDef.getTitle());
        assertEquals(3, formDef.getChildren().size());
        assertEquals("What is your first name?", formDef.getChild(0).getLabelInnerText());
    }

    @Test
    public void parsesPreloadForm() throws IOException {
        FormDef formDef = parse(r("Sample-Preloading.xml")).formDef;
        assertEquals("Sample Form - Preloading", formDef.getTitle());
    }

    @Test public void parsesSecondaryInstanceForm() throws IOException, XPathSyntaxException {
        FormDef formDef = parse(SECONDARY_INSTANCE_XML).formDef;
        assertEquals("Form with secondary instance", formDef.getTitle());
    }

    @Test public void parsesExternalSecondaryInstanceForm() throws IOException, XPathSyntaxException {
        FormDef formDef = parse(EXTERNAL_SECONDARY_INSTANCE_XML).formDef;
        assertEquals("Form with external secondary instance", formDef.getTitle());
        TreeReference treeReference = ((XPathPathExpr)
                parseXPath("instance('towns')/data_set")).getReference();
        EvaluationContext evaluationContext = formDef.getEvaluationContext();
        List<TreeReference> treeReferences = evaluationContext.expandReference(treeReference);
        assertEquals(1, treeReferences.size());
        DataInstance townInstance = formDef.getNonMainInstance("towns");
        AbstractTreeElement tiRoot = townInstance.getRoot();
        assertEquals("towndata", tiRoot.getName());
        assertEquals(1, tiRoot.getNumChildren());
        AbstractTreeElement dataSetChild = tiRoot.getChild("data_set", 0);
        assertEquals("us_east", dataSetChild.getValue().getDisplayText());
    }

    @Test public void timesParsingLargeInternalSecondaryInstanceFiles() throws IOException, XPathSyntaxException {
        timeParsing(new LargeIsiFileGenerator(SECONDARY_INSTANCE_XML), SECONDARY_INSTANCE_LARGE_XML,
                SECONDARY_INSTANCE_LARGE_XML);
    }

    @Test public void timesParsingLargeExternalSecondaryInstanceFiles() throws IOException, XPathSyntaxException {
        timeParsing(new LargeEsiFileGenerator(), r("towns-large.xml"), EXTERNAL_SECONDARY_INSTANCE_LARGE_XML);
    }

    /**
     * In a loop, parses forms with increasingly larger external secondary instance files. Writes timing results
     * to the console.
     *
     * @param lfg a file generator
     * @param largeDataFilename the name to be given to the generated file
     * @param parseFilename the name of the file to parse
     * @throws IOException if there are problems reading or writing files
     */
    private void timeParsing(LargeInstanceFileGenerator lfg, Path largeDataFilename, Path parseFilename) throws IOException {
        NumberFormat nf = NumberFormat.getNumberInstance();
        List<String> results = new ArrayList<>(); // Collect and display at end
        results.add("Children\tSeconds");
        for (double powerOfTen = 3; powerOfTen <= 4.0; powerOfTen += 0.1) {  // Raise this upper limit to really measure
            int numChildren = (int) Math.pow(10, powerOfTen);
            lfg.createLargeInstanceSource(largeDataFilename, numChildren);
            long startMs = System.currentTimeMillis();
            parse(parseFilename);
            double elapsed = (System.currentTimeMillis() - startMs) / 1000.0;
            results.add(nf.format(numChildren) + "\t" + nf.format(elapsed));
            if (elapsed > 5.0) { // Make this larger if needed
                break;
            }
        }
        for (String line : results) {
            System.out.println(line);
        }
        Files.delete(largeDataFilename);
    }

    @Test public void multipleInstancesFormSavesAndRestores() throws IOException, DeserializationException {
        serAndDeserializeForm(r("Simpler_Cascading_Select_Form.xml"));
    }

    @Test public void externalSecondaryInstanceFormSavesAndRestores() throws IOException, DeserializationException {
        serAndDeserializeForm(EXTERNAL_SECONDARY_INSTANCE_XML);
    }

    private void serAndDeserializeForm(Path formName) throws IOException, DeserializationException {
        initSerialization();
        FormDef formDef = parse(formName).formDef;
        Path p = Files.createTempFile("serialized-form", null);

        final DataOutputStream dos = new DataOutputStream(Files.newOutputStream(p));
        formDef.writeExternal(dos);
        dos.close();

        final DataInputStream dis = new DataInputStream(Files.newInputStream(p));
        formDef.readExternal(dis, defaultPrototypes());
        dis.close();

        Files.delete(p);
    }

    private void initSerialization() {
        final String[] SERIALIZABLE_CLASSES = { // Copied from Collect application
                "org.javarosa.core.services.locale.ResourceFileDataSource",
                "org.javarosa.core.services.locale.TableLocaleSource",
                "org.javarosa.core.model.FormDef",
                "org.javarosa.core.model.SubmissionProfile",
                "org.javarosa.core.model.QuestionDef",
                "org.javarosa.core.model.GroupDef",
                "org.javarosa.core.model.instance.FormInstance",
                "org.javarosa.core.model.instance.ExternalDataInstance", // Todo export this structure to Collect and remove from there.
                "org.javarosa.core.model.data.MultiPointerAnswerData",
                "org.javarosa.core.model.data.PointerAnswerData",
                "org.javarosa.core.model.data.SelectMultiData",
                "org.javarosa.core.model.data.SelectOneData",
                "org.javarosa.core.model.data.StringData",
                "org.javarosa.core.model.data.TimeData",
                "org.javarosa.core.model.data.UncastData",
                "org.javarosa.core.model.data.helper.BasicDataPointer",
                "org.javarosa.core.model.Action",
                "org.javarosa.core.model.actions.SetValueAction"
        };
        PrototypeManager.registerPrototypes(SERIALIZABLE_CLASSES);
        new XFormsModule().registerModule();
    }

    @Test
    public void parsesRangeForm() throws IOException {
        FormDef formDef = parse(r("range-form.xml")).formDef;
        RangeQuestion question = (RangeQuestion) formDef.getChild(0);
        assertEquals(CONTROL_RANGE, question.getControlType());
        assertEquals(-2.0d, question.getRangeStart().doubleValue(), 0);
        assertEquals( 2.0d, question.getRangeEnd()  .doubleValue(), 0);
        assertEquals( 0.5d, question.getRangeStep() .doubleValue(), 0);
    }

    @Test(expected = XFormParseException.class)
    public void throwsParseExceptionOnBadRangeForm() throws IOException {
        parse(r("bad-range-form.xml"));
    }

    @Test
    public void parsesMetaNamespaceForm() throws IOException {
        ParseResult parseResult = parse(r("meta-namespace-form.xml"));
        assertEquals(parseResult.formDef.getTitle(), "Namespace for Metadata");
        assertEquals("Number of error messages", 0, parseResult.errorMessages.size());
    }

    @Test
    public void serializeAndRestoreMetaNamespaceFormInstance() throws IOException {
        // Given
        ParseResult parseResult = parse(r("meta-namespace-form.xml"));
        assertEquals(parseResult.formDef.getTitle(), "Namespace for Metadata");
        assertEquals("Number of error messages", 0, parseResult.errorMessages.size());

        FormDef formDef = parseResult.formDef;
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
        assertEquals(null, audit3.getNamespacePrefix());
        assertEquals(null, audit3.getNamespace());

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
        assertEquals(null, audit3.getNamespacePrefix());
        assertEquals(null, audit3.getNamespace());
        assertEquals(AUDIT_3_ANSWER, audit3.getValue().getValue());
    }

    private ParseResult parse(Path formName) throws IOException {
        XFormParser parser = new XFormParser(new FileReader(formName.toString()));
        final List<String> errorMessages = new ArrayList<>();
        parser.reporter = new XFormParserReporter() {
            @Override
            public void warning(String type, String message, String xmlLocation) {
                errorMessages.add(message);
                super.warning(type, message, xmlLocation);
            }

            @Override
            public void error(String message) {
                errorMessages.add(message);
                super.error(message);
            }
        };
        return new ParseResult(parser.parse(), errorMessages);
    }

    class ParseResult {
        final FormDef formDef;
        final List<String> errorMessages;

        ParseResult(FormDef formDef, List<String> errorMessages) {
            this.formDef = formDef;
            this.errorMessages = errorMessages;
        }
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

    /** Generates large versions of a secondary instance */
    public interface LargeInstanceFileGenerator {
        /** Creates a large instance file with the given name, and the given number of children */
        void createLargeInstanceSource(Path outputFilename, int numChildren) throws IOException;
    }

    /** Generates large versions of an external secondary instance, from scratch */
    class LargeEsiFileGenerator implements LargeInstanceFileGenerator {
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

    /** Generates large versions of a file with an internal secondary instance, using a template */
    class LargeIsiFileGenerator implements LargeInstanceFileGenerator {
        private Path templateFilename;

        LargeIsiFileGenerator(Path templateFilename) {
            this.templateFilename = templateFilename;
        }

        @Override
        public void createLargeInstanceSource(Path outputFilename, int numChildren) throws IOException {
            BufferedReader br = Files.newBufferedReader(templateFilename, Charset.defaultCharset());
            PrintWriter pw = new PrintWriter(outputFilename.toString());
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().startsWith("<data_set>")) {
                    // The one instance of this in the template is replaced with multiple lines
                    for (int i = 0; i < numChildren; ++i) {
                        pw.println("<data_set>us_east</data_set>");
                    }
                } else {
                    pw.println(line);
                }
            }
            pw.close();
        }
    }
}
