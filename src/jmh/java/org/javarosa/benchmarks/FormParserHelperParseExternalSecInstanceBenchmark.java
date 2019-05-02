package org.javarosa.benchmarks;

import org.javarosa.xform.parse.FormParserHelper;
import org.javarosa.xform.parse.IXFormParserFactory;
import org.javarosa.xform.parse.XFormParserFactory;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.nio.file.Path;

import static org.javarosa.benchmarks.BenchmarkUtils.dryRun;
import static org.javarosa.benchmarks.BenchmarkUtils.getNigeriaWardsXMLWithExternal2ndryInstance;

public class FormParserHelperParseExternalSecInstanceBenchmark {
    public static void main(String[] args) {
        dryRun(FormParserHelperParseExternalSecInstanceBenchmark.class);
    }

    @State(Scope.Thread)
    public static class FormParserHelperParseExternalSecondaryInstanceState {
        Path xFormFilePath;

        private static IXFormParserFactory _factory = new XFormParserFactory();

        public static IXFormParserFactory setXFormParserFactory(IXFormParserFactory factory) {
            IXFormParserFactory oldFactory = _factory;
            _factory = factory;
            return oldFactory;
        }

        @Setup(Level.Trial)
        public void initialize() {
            xFormFilePath = getNigeriaWardsXMLWithExternal2ndryInstance();
        }
    }

    //@Benchmark
    public void
    benchmark_FormParserHelper_parse_external_secondary_instance(FormParserHelperParseExternalSecondaryInstanceState state, Blackhole bh) throws IOException {
        bh.consume(FormParserHelper.parse(state.xFormFilePath));
    }

}
