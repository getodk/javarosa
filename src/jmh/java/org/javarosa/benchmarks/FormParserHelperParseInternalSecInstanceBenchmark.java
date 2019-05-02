package org.javarosa.benchmarks;

import org.javarosa.xform.parse.FormParserHelper;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.nio.file.Path;

import static org.javarosa.benchmarks.BenchmarkUtils.dryRun;
import static org.javarosa.benchmarks.BenchmarkUtils.getNigeriaWardsXMLWithInternal2ndryInstance;

public class FormParserHelperParseInternalSecInstanceBenchmark {
    public static void main(String[] args) {
        dryRun(FormParserHelperParseInternalSecInstanceBenchmark.class);
    }

    @State(Scope.Thread)
    public static class FormParserHelperParseInternalSecondaryInstanceState {
        Path xFormFilePath;

        @Setup(Level.Trial)
        public void initialize() {
            xFormFilePath = getNigeriaWardsXMLWithInternal2ndryInstance();
        }
    }

     @Benchmark
    public void
    benchmark_FormParserHelper_parse_internal_secondary_instance(FormParserHelperParseInternalSecondaryInstanceState state, Blackhole bh) throws IOException {
        bh.consume(FormParserHelper.parse(state.xFormFilePath));
    }
}
