package org.javarosa.benchmarks;

import static org.javarosa.benchmarks.BenchmarkUtils.*;
import static org.javarosa.benchmarks.BenchmarkUtils.prepareAssets;
import static org.javarosa.core.reference.ReferenceManagerTestUtils.setUpSimpleReferenceManager;

import java.io.IOException;
import java.nio.file.Path;
import org.javarosa.xform.parse.FormParserHelper;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

public class FormParserHelperParseExternalSecInstanceBenchmark {
    public static void main(String[] args) {
        dryRun(FormParserHelperParseExternalSecInstanceBenchmark.class);
    }

    @State(Scope.Thread)
    public static class FormParserHelperParseExternalSecondaryInstanceState {
        Path xFormFilePath;

        @Setup(Level.Trial)
        public void initialize() {
            Path assetsPath = prepareAssets("nigeria_wards_external.xml", "lgas.xml", "wards.xml");
            xFormFilePath = assetsPath.resolve("nigeria_wards_external.xml");
            setUpSimpleReferenceManager("file", assetsPath);
        }
    }

    @Benchmark
    public void
    benchmark_FormParserHelper_parse_external_secondary_instance(FormParserHelperParseExternalSecondaryInstanceState state, Blackhole bh) throws IOException {
        bh.consume(FormParserHelper.parse(state.xFormFilePath));
    }

}
