package org.javarosa.benchmarks;

import org.javarosa.core.reference.InvalidReferenceException;
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

public class XFormParserBenchmark {
    public static void main(String[] args) {
        dryRun(XFormParserBenchmark.class);
    }

    @State(Scope.Thread)
    public static class XFormParserBenchmarkState {
        Path xFormInternalSecondaryInstancesMinified;
        Path xFormInternalSecondaryInstances;
        Path xFormExternalSecondaryInstances;
        Path lgaSecondaryInstance;
        Path wardExternalSecondaryInstance;

        @Setup(Level.Trial)
        public void
        initialize() throws FileNotFoundException {
            xFormInternalSecondaryInstancesMinified = BenchmarkUtils.getMinifiedNigeriaWardsXMLWithInternal2ndryInstance();
            xFormInternalSecondaryInstances = BenchmarkUtils.getNigeriaWardsXMLWithInternal2ndryInstance();
            xFormExternalSecondaryInstances = BenchmarkUtils.getNigeriaWardsXMLWithExternal2ndryInstance();
            lgaSecondaryInstance = BenchmarkUtils.getLGAsExternalInstance();
            wardExternalSecondaryInstance = BenchmarkUtils.getWardsExternalInstance();
        }
    }

    @Benchmark
    public void
    benchmarkParseExternalInstanceXFormOnly(XFormParserBenchmarkState state, Blackhole bh)
        throws IOException, XmlPullParserException, InvalidReferenceException,
        UnfullfilledRequirementsException, InvalidStructureException {
        Reader reader = new FileReader(state.xFormInternalSecondaryInstancesMinified.toFile());
        Document kxmlDocument = org.javarosa.xform.parse.XFormParser.getXMLDocument(reader);
        bh.consume(kxmlDocument);
    }

    @Benchmark
    public void
    benchmarkParseInternalInstanceXForm(XFormParserBenchmarkState state, Blackhole bh)
        throws IOException, XmlPullParserException, InvalidReferenceException,
        UnfullfilledRequirementsException, InvalidStructureException {
        Reader reader = new FileReader(state.xFormInternalSecondaryInstances.toFile());
        Document kxmlDocument = org.javarosa.xform.parse.XFormParser.getXMLDocument(reader);
        bh.consume(kxmlDocument);
    }


    @Benchmark
    public void
    benchmarkParseExternalInstanceLGA(XFormParserBenchmarkState state, Blackhole bh)
        throws IOException, XmlPullParserException, InvalidReferenceException,
        UnfullfilledRequirementsException, InvalidStructureException {
        Reader reader = new FileReader(state.lgaSecondaryInstance.toFile());
        Document kxmlDocument = org.javarosa.xform.parse.XFormParser.getXMLDocument(reader);
        bh.consume(kxmlDocument);
    }

    @Benchmark
    public void
    benchmarkParseExternalInstanceWards(XFormParserBenchmarkState state, Blackhole bh)
        throws IOException, XmlPullParserException, InvalidReferenceException,
        UnfullfilledRequirementsException, InvalidStructureException {
        Reader reader = new FileReader(state.wardExternalSecondaryInstance.toFile());
        Document kxmlDocument = org.javarosa.xform.parse.XFormParser.getXMLDocument(reader);
        bh.consume(kxmlDocument);
    }

    @Benchmark
    public void
    benchmarkParseExternalInstanceXFormWithInstanceFiles(XFormParserBenchmarkState state, Blackhole bh)
        throws IOException, XmlPullParserException, InvalidReferenceException,
        UnfullfilledRequirementsException, InvalidStructureException {

        Reader externalInstanceXFormReader = new FileReader(state.xFormExternalSecondaryInstances.toFile());
        Reader lgaSecondaryInstanceReader = new FileReader(state.lgaSecondaryInstance.toFile());
        Reader wardsSecondaryInstanceReader = new FileReader(state.wardExternalSecondaryInstance.toFile());
        Document externalXFormInstanceDocument = org.javarosa.xform.parse.XFormParser.getXMLDocument(externalInstanceXFormReader);
        Document lgaExternalInstanceDocument = org.javarosa.xform.parse.XFormParser.getXMLDocument(lgaSecondaryInstanceReader);
        Document wardExternalInstanceDocument = org.javarosa.xform.parse.XFormParser.getXMLDocument(wardsSecondaryInstanceReader);

        bh.consume(externalXFormInstanceDocument);
        bh.consume(lgaExternalInstanceDocument);
        bh.consume(wardExternalInstanceDocument);

    }
 

}
