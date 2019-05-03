package org.javarosa.benchmarks;

import org.javarosa.core.model.FormDef;
import org.javarosa.xform.parse.FormParserHelper;
import org.javarosa.xform.parse.IXFormParserFactory;
import org.javarosa.xform.parse.XFormParserFactory;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.nio.file.Path;

import static org.javarosa.benchmarks.BenchmarkUtils.dryRun;
import static org.javarosa.benchmarks.BenchmarkUtils.getNigeriaWardsXMLWithExternal2ndryInstance;
import static org.javarosa.benchmarks.BenchmarkUtils.getNigeriaWardsXMLWithInternal2ndryInstance;
import static org.javarosa.benchmarks.BenchmarkUtils.getMinifiedNigeriaWardsXMLWithInternal2ndryInstance;

public class FormDefParserBenchmark {
    public static void main(String[] args) {
        dryRun(FormDefParserBenchmark.class);
    }

    @State(Scope.Thread)
    public static class FormDefParserState {
        Path xFormExternalInstanceFilePath;
        Path xFormInternalInstanceFilePath;
        Path xFormMinifiedInternalInstanceFilePath;

        private static IXFormParserFactory _factory = new XFormParserFactory();

        public static IXFormParserFactory setXFormParserFactory(IXFormParserFactory factory) {
            IXFormParserFactory oldFactory = _factory;
            _factory = factory;
            return oldFactory;
        }

        @Setup(Level.Trial)
        public void initialize() {
            xFormExternalInstanceFilePath = getNigeriaWardsXMLWithExternal2ndryInstance();
            xFormInternalInstanceFilePath = getNigeriaWardsXMLWithInternal2ndryInstance();
            xFormMinifiedInternalInstanceFilePath = getMinifiedNigeriaWardsXMLWithInternal2ndryInstance();
        }
    }

    @Benchmark
    public void
    benchmarkParseExternalSecondaryInstance(FormDefParserState state, Blackhole bh) throws IOException {
        FormDef formDef = FormParserHelper.parse(state.xFormExternalInstanceFilePath);
        bh.consume(formDef);
    }


    @Benchmark
    public void
    benchmarkParseInternalSecondaryInstance(FormDefParserState state, Blackhole bh) throws IOException {
        FormDef formDef = FormParserHelper.parse(state.xFormInternalInstanceFilePath);
        bh.consume(formDef);
    }


    @Benchmark
    public void
    benchmarkParseMinifiedInternalSecondaryInstance(FormDefParserState state, Blackhole bh) throws IOException {
        FormDef formDef = FormParserHelper.parse(state.xFormMinifiedInternalInstanceFilePath);
        bh.consume(formDef);
    }

}
