package org.javarosa.benchmarks;

import static java.nio.file.Files.readAllBytes;
import static org.javarosa.benchmarks.BenchmarkUtils.dryRun;
import static org.javarosa.benchmarks.BenchmarkUtils.prepareAssets;
import static org.javarosa.core.reference.ReferenceManagerTestUtils.setUpSimpleReferenceManager;

import java.io.IOException;
import java.nio.file.Path;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

public class BenchmarkTemplate {
    public static void main(String[] args) {
        // You can dry-run all the benchmarks in this class with BenchmarkUtils.dryRun()
        // This won't take any measure.
        // It'll just run the benchmark methods to ensure everything works as intended.
        // It will let you debug the benchmark code too.
        dryRun(BenchmarkTemplate.class);
    }

    @State(Scope.Thread)
    public static class StateClassTemplate {
        Path formFile;

        @Setup(Level.Trial)
        public void setUp() {
            // When the benchmark needs files from src/jmh/resources, you need to call BenchmarkUtils.prepareAssets()
            // to take them out of the JAR file JMH uses into a temporary directory that JavaRosa can work with.
            Path assetsPath = prepareAssets("some-file.xml");
            // Once you do so, get paths to any individual file using Path.resolve
            formFile = assetsPath.resolve("some-file.xml");
            // If your forms load external files, you'll need to prime the ResourceManager using the assetsDir
            // returned by BenchmarkUtils.prepareAssets(). You need to make sure that all external files in the
            // form are like jr://file/filename (all use the "file" hostname, all are located at the root).
            setUpSimpleReferenceManager(assetsPath, "file");
        }
    }

    @Benchmark
    public void benchmark_method_template(StateClassTemplate state, Blackhole bh) throws IOException {
        // Always use Blackhole.consume with the output of the benchmarked action
        bh.consume(readAllBytes(state.formFile));
    }
}
