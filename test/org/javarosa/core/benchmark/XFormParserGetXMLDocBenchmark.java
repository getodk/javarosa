package org.javarosa.core.benchmark;

import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManagerTestUtils;
import org.javarosa.core.util.PathConst;
import org.javarosa.xform.parse.XFormParser;
import org.javarosa.xml.util.InvalidStructureException;
import org.javarosa.xml.util.UnfullfilledRequirementsException;
import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.NoBenchmarksException;
import org.openjdk.jmh.runner.Runner;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;

public class XFormParserGetXMLDocBenchmark {

    @Test
    public void
    launchBenchmark() throws Exception {
        try{
            RunResult run = new Runner(BenchmarkUtils.getJVMOptions(this.getClass().getName())).run().iterator().next();

        }catch (NoBenchmarksException nbe){

        }
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
            Path resourcePath = PathConst.getTestResourcePath().toPath();
            ReferenceManagerTestUtils.setUpSimpleReferenceManager("file", resourcePath);
            xFormWithOnlyInternalSecondaryInstances = BenchmarkUtils.getNigeriaWardsXMLWithInternal2ndryInstance().toPath();
            xFormWithInternalAndExternalSecondayInstances = BenchmarkUtils.getNigeriaWardsXMLWithExternal2ndryInstance().toPath();
            lgaSecondaryInstance = BenchmarkUtils.getNigeriaWardsWardsLGAsInstance().toPath();
            wardExternalSecondaryInstance = BenchmarkUtils.getNigeriaWardsWardsXMLInstance().toPath();


        }
    }

    @Benchmark
    public void
    benchmark_XFormParser_reader1(XFormParserState state, Blackhole bh)
        throws IOException, XmlPullParserException, InvalidReferenceException,
        UnfullfilledRequirementsException, InvalidStructureException {
       Reader reader1 = new FileReader(state.xFormWithOnlyInternalSecondaryInstances.toFile());
       bh.consume(XFormParser.getXMLDocument(reader1));

    }

    @Benchmark
    public void
    benchmark_XFormParser_reader2(XFormParserState state, Blackhole bh)
        throws IOException, XmlPullParserException, InvalidReferenceException,
        UnfullfilledRequirementsException, InvalidStructureException {

        Reader reader2 = new FileReader(state.xFormWithInternalAndExternalSecondayInstances.toFile());
        Reader reader3 = new FileReader(state.lgaSecondaryInstance.toFile());
        Reader reader4 = new FileReader(state.wardExternalSecondaryInstance.toFile());
        bh.consume(XFormParser.getXMLDocument(reader2));
        bh.consume(XFormParser.getXMLDocument(reader3));
        bh.consume(XFormParser.getXMLDocument(reader4));

    }

}
