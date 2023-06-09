package org.javarosa.benchmarks;

import static org.javarosa.benchmarks.BenchmarkUtils.dryRun;
import static org.javarosa.benchmarks.BenchmarkUtils.getNigeriaWardsXMLWithInternal2ndryInstance;

import java.io.IOException;
import java.nio.file.Path;
import org.javarosa.xform.parse.FormParserHelper;
import org.javarosa.xform.parse.ParseException;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

public class FormParserHelperParseInternalInstanceBenchmark {
    public static void main(String[] args) {
        dryRun(FormParserHelperParseInternalInstanceBenchmark.class);
    }

    @State(Scope.Thread)
    public static class FormParserHelperParseInternalInstanceBenchmarkState {
        Path xFormFilePath;

        @Setup(Level.Trial)
        public void initialize() {
            xFormFilePath = getNigeriaWardsXMLWithInternal2ndryInstance();
        }
    }

    @Benchmark
    public void
    benchmarkParseInternalSecondaryInstanceForm(FormParserHelperParseInternalInstanceBenchmarkState state, Blackhole bh) throws IOException, ParseException {
        bh.consume(FormParserHelper.parse(state.xFormFilePath));
    }
}
