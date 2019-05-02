package org.javarosa.benchmarks;

import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.XmlXFormInstance;
import org.javarosa.xml.util.InvalidStructureException;
import org.javarosa.xml.util.UnfullfilledRequirementsException;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.nio.file.Path;

import static java.nio.file.Files.readAllBytes;
import static org.javarosa.benchmarks.BenchmarkUtils.*;
import static org.javarosa.core.reference.ReferenceManagerTestUtils.setUpSimpleReferenceManager;

public class XmlExternalInstanceBenchmark {
    public static void main(String[] args) {
        dryRun(XmlExternalInstanceBenchmark.class);
    }

    @State(Scope.Thread)
    public static class XmlExternalInstanceState {
        String xFormFilePath;
        @Setup(Level.Trial)
        public void initialize() {

            xFormFilePath = getNigeriaWardsXMLWithInternal2ndryInstance().toString();

        }
    }

     @Benchmark
    public void XmlXFormInstance_parse_internal_secondary_instance(XmlExternalInstanceState state, Blackhole bh) throws IOException, UnfullfilledRequirementsException, XmlPullParserException, InvalidStructureException {
        TreeElement documentRootTreeElement = XmlXFormInstance.parse("nigeriawards", state.xFormFilePath);

        l("-----------"+documentRootTreeElement
            .getChildAt(0)
            .getChildAt(1)
            .getChildAt(0)
            .getChildAt(0)
        .getNumChildren());
        bh.consume(documentRootTreeElement);
    }
}
