package org.javarosa.benchmarks;

import static org.javarosa.benchmarks.BenchmarkUtils.dryRun;
import static org.javarosa.benchmarks.BenchmarkUtils.getMinifiedNigeriaWardsXMLWithInternal2ndryInstance;
import static org.javarosa.benchmarks.BenchmarkUtils.getNigeriaWardsXMLWithExternal2ndryInstance;

import java.io.IOException;
import java.nio.file.Path;
import org.javarosa.xform.parse.FormParserHelper;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

public class FormParserHelperParseInternalInstanceMinifiedBenchmark {
    public static void main(String[] args) {
        dryRun(FormParserHelperParseInternalInstanceMinifiedBenchmark.class);
    }

    @State(Scope.Thread)
    public static class FormParserHelperParseInternalInstanceMinifiedBenchmarkState {
        Path xFormFilePath;

        @Setup(Level.Trial)
        public void initialize() {
            xFormFilePath = getMinifiedNigeriaWardsXMLWithInternal2ndryInstance();
        }
    }

    @Benchmark
    public void
    benchmarkParseInternalInstanceFormMinified(FormParserHelperParseInternalInstanceMinifiedBenchmarkState state, Blackhole bh) throws IOException {
        bh.consume(FormParserHelper.parse(state.xFormFilePath));
    }

}
