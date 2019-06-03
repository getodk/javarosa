package org.javarosa.benchmarks;

import org.javarosa.xform.parse.FormParserHelper;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.nio.file.Path;

import static org.javarosa.benchmarks.BenchmarkUtils.dryRun;
import static org.javarosa.benchmarks.BenchmarkUtils.prepareAssets;
import static org.javarosa.core.reference.ReferenceManagerTestUtils.setUpSimpleReferenceManager;

public class FormParserHelperParseInternalSecInstanceBenchmark {
    public static void main(String[] args) {
        dryRun(FormParserHelperParseInternalSecInstanceBenchmark.class);
    }

    @State(Scope.Thread)
    public static class FormParserHelperParseInternalSecondaryInstanceState {
        Path xFormFilePath;

        @Setup(Level.Trial)
        public void initialize() {
            Path assetsDir = prepareAssets("nigeria_wards_external_combined.xml");
            xFormFilePath = assetsDir.resolve("nigeria_wards_external_combined.xml");
            setUpSimpleReferenceManager("file", assetsDir);
        }
    }

    @Benchmark
    public void
    benchmark_FormParserHelper_parse_internal_secondary_instance(FormParserHelperParseInternalSecondaryInstanceState state, Blackhole bh) throws IOException {
        bh.consume(FormParserHelper.parse(state.xFormFilePath));
    }
}
