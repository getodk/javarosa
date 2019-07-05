package org.javarosa.xform.parse;

import org.javarosa.core.model.CoreModelModule;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.services.PrototypeManager;
import org.javarosa.core.util.JavaRosaCoreModule;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.model.xform.XFormsModule;
import org.javarosa.xform.util.XFormUtils;
import org.slf4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static org.javarosa.core.reference.ReferenceManagerTestUtils.setUpSimpleReferenceManager;
import static org.javarosa.core.util.externalizable.ExtUtil.defaultPrototypes;

public final class FormParserHelper {

    private FormParserHelper() {
    }

    public static FormDef parse(Path formName) throws IOException {
        return XFormUtils.getFormFromInputStream(new FileInputStream(formName.toString()));
    }

    public static FormDef parse(Path formName, String lastSavedSrc) throws IOException {
        return XFormUtils.getFormFromInputStream(new FileInputStream(formName.toString()), lastSavedSrc);
    }

    /**
     * In a loop, parses forms with increasingly larger external secondary instance files. Writes timing results
     * to the console.
     *
     * @param lfg               a file generator
     * @param largeDataFilename the name to be given to the generated file
     * @param parseFilename     the name of the file to parse
     * @param logger            the logger to send output to
     * @throws IOException if there are problems reading or writing files
     */
    static void timeParsing(LargeInstanceFileGenerator lfg, Path largeDataFilename, Path parseFilename, Logger logger) throws IOException {
        setUpSimpleReferenceManager(largeDataFilename.getParent(), "file");
        NumberFormat nf = NumberFormat.getNumberInstance();
        List<String> results = new ArrayList<>(); // Collect and display at end
        results.add("Children\tSeconds");
        for (double powerOfTen = 3; powerOfTen <= 4.0; powerOfTen += 0.1) {  // Raise this upper limit to really measure
            int numChildren = (int) Math.pow(10, powerOfTen);
            lfg.createLargeInstanceSource(largeDataFilename, numChildren);
            long startMs = System.currentTimeMillis();
            FormParserHelper.parse(parseFilename);
            double elapsed = (System.currentTimeMillis() - startMs) / 1000.0;
            results.add(nf.format(numChildren) + "\t" + nf.format(elapsed));
            if (elapsed > 5.0) { // Make this larger if needed
                break;
            }
        }
        for (String line : results) {
            logger.info(line);
        }
        Files.delete(largeDataFilename);
    }

    static void serAndDeserializeForm(Path formName) throws IOException, DeserializationException {
        initSerialization();
        FormDef formDef = parse(formName);
        Path p = Files.createTempFile("serialized-form", null);

        final DataOutputStream dos = new DataOutputStream(Files.newOutputStream(p));
        formDef.writeExternal(dos);
        dos.close();

        final DataInputStream dis = new DataInputStream(Files.newInputStream(p));
        formDef.readExternal(dis, defaultPrototypes());
        dis.close();

        Files.delete(p);
    }

    private static void initSerialization() {
        PrototypeManager.registerPrototypes(JavaRosaCoreModule.classNames);
        PrototypeManager.registerPrototypes(CoreModelModule.classNames);
        new XFormsModule().registerModule();
    }

    /**
     * Generates large versions of a secondary instance
     */
    interface LargeInstanceFileGenerator {
        /**
         * Creates a large instance file with the given name, and the given number of children
         */
        void createLargeInstanceSource(Path outputFilename, int numChildren) throws IOException;
    }
}
