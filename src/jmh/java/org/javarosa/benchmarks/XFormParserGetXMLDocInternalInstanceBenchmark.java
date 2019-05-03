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

public class XFormParserGetXMLDocInternalInstanceBenchmark {
    public static void main(String[] args) {
        dryRun(XFormParserGetXMLDocInternalInstanceBenchmark.class);
    }

    @State(Scope.Thread)
    public static class XFormParserState {
        Path xFormPath;
        @Setup(Level.Trial)
        public void
        initialize() throws FileNotFoundException {
            xFormPath = BenchmarkUtils.getNigeriaWardsXMLWithExternal2ndryInstance();


        }
    }

    @Benchmark
    public void
    benchmark_XFormParser_parse_document_internal_2ndry_instance(XFormParserState state, Blackhole bh)
        throws IOException, XmlPullParserException, InvalidReferenceException,
        UnfullfilledRequirementsException, InvalidStructureException {
       Reader xFormReader = new FileReader(state.xFormPath.toFile());
       Document xFormXMLDocument = XFormParser.getXMLDocument(xFormReader);
       bh.consume(xFormXMLDocument);
    }

}
