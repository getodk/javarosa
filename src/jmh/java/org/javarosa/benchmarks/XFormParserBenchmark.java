package org.javarosa.benchmarks;

import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.xform.parse.XFormParser;
import org.javarosa.xml.util.InvalidStructureException;
import org.javarosa.xml.util.UnfullfilledRequirementsException;
import org.kxml2.kdom.Document;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;

import static org.javarosa.benchmarks.BenchmarkUtils.dryRun;

public class XFormParserGetXMLDocExternalInstanceBenchmark {
    public static void main(String[] args) {
        dryRun(XFormParserGetXMLDocExternalInstanceBenchmark.class);
    }

    @State(Scope.Thread)
    public static class XFormParserState {
        Path xFormWithOnlyInternalSecondaryInstances;
        Path xFormWithInternalAndExternalSecondayInstances;
        Path lgaSecondaryInstance;
        Path wardExternalSecondaryInstance;
        @Setup(Level.Trial)
        public void
        initialize() throws FileNotFoundException {
            xFormWithOnlyInternalSecondaryInstances = BenchmarkUtils.getNigeriaWardsXMLWithInternal2ndryInstance();
            xFormWithInternalAndExternalSecondayInstances = BenchmarkUtils.getNigeriaWardsXMLWithExternal2ndryInstance();
            lgaSecondaryInstance = BenchmarkUtils.getLGAsExternalInstance();
            wardExternalSecondaryInstance = BenchmarkUtils.getWardsExternalInstance();


        }
    }

    @Benchmark
    public void
    benchmark_XFormParser_parse_kxml_document_internal_2ndry_instance(XFormParserState state, Blackhole bh)
        throws IOException, XmlPullParserException, InvalidReferenceException,
        UnfullfilledRequirementsException, InvalidStructureException {
       Reader internalInstanceXFormReader = new FileReader(state.xFormWithOnlyInternalSecondaryInstances.toFile());
       Document xFormXMLDocument = XFormParser.getXMLDocument(internalInstanceXFormReader);
       bh.consume(xFormXMLDocument);
    }

      @Benchmark
    public void
    benchmark_XFormParser_parse_kxml_document_external_2ndry_instance(XFormParserState state, Blackhole bh)
        throws IOException, XmlPullParserException, InvalidReferenceException,
        UnfullfilledRequirementsException, InvalidStructureException {

        Reader externalInstanceXFormReader = new FileReader(state.xFormWithInternalAndExternalSecondayInstances.toFile());
        Reader lgaSecondaryInstanceReader = new FileReader(state.lgaSecondaryInstance.toFile());
        Reader wardsSecondaryInstanceReader = new FileReader(state.wardExternalSecondaryInstance.toFile());
        Document externalXFormInstanceDocument = XFormParser.getXMLDocument(externalInstanceXFormReader);
        Document lgaExternalInstanceDocument = XFormParser.getXMLDocument(lgaSecondaryInstanceReader);
        Document wardExternalInstanceDocument = XFormParser.getXMLDocument(wardsSecondaryInstanceReader);

        bh.consume(externalXFormInstanceDocument);
        bh.consume(lgaExternalInstanceDocument);
        bh.consume(wardExternalInstanceDocument);


    }

}
