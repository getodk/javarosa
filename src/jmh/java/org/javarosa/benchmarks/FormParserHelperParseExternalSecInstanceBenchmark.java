package org.javarosa.benchmarks;

import org.javarosa.xform.parse.FormParserHelper;
import org.openjdk.jmh.annotations.*;
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

        @Setup(Level.Trial)
        public void initialize() {
            xFormFilePath = getNigeriaWardsXMLWithExternal2ndryInstance();
        }
    }

     @Benchmark
    public void
    benchmark_FormParserHelper_parse_external_secondary_instance(FormParserHelperParseExternalSecondaryInstanceState state, Blackhole bh) throws IOException {
        bh.consume(FormParserHelper.parse(state.xFormFilePath));
    }

}
