package org.javarosa.core.model.instance.test;


import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import org.javarosa.core.PathConst;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.test.FormParseInit;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.xform.parse.XFormParser;
import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

public class TreeElementBenchmark {
    @Test
    public void launchBenchmark() throws Exception {
        Options opt = new OptionsBuilder()
            // Specify which benchmarks to run.
            // You can be more specific if you'd like to run only one benchmark per test.
            .include(this.getClass().getName() + ".*")
            // Set the following options as needed
            .mode(Mode.AverageTime)
            .timeUnit(TimeUnit.MICROSECONDS)
            .warmupTime(TimeValue.seconds(1))
            .warmupIterations(2)
            .measurementTime(TimeValue.seconds(1))
            .measurementIterations(2)
            .threads(2)
            .forks(1)
            .shouldFailOnError(true)
            .shouldDoGC(true)
            //.jvmArgs("-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintInlining")
            //.addProfiler(WinPerfAsmProfiler.class)
            .build();

        new Runner(opt).run().iterator().next();
    }

    @State(Scope.Thread)
    public static class BenchmarkState {
        private TreeElement dataRootNode;
        private TreeElement savedRoot;
        private FormDef formDef;

        @Setup(Level.Trial)
        public void
        initialize() {
            FormParseInit formParseInit = new FormParseInit(r("populate-nodes-attributes.xml"));

            FormEntryController formEntryController = formParseInit.getFormEntryController();

            byte[] formInstanceAsBytes = null;
            try {
                formInstanceAsBytes = Files.readAllBytes(Paths.get(PathConst.getTestResourcePath().getAbsolutePath(), "populate-nodes-attributes-instance.xml"));
            } catch (IOException e) {
                fail("There was a problem with reading the test data.\n" + e.getMessage());
            }
            savedRoot = XFormParser.restoreDataModel(formInstanceAsBytes, null).getRoot();
            formDef = formEntryController.getModel().getForm();
            dataRootNode = formDef.getInstance().getRoot().deepCopy(true);
        }
    }

    @Benchmark
    public void benchmark_1(BenchmarkState state, Blackhole bh) {
        state.dataRootNode.populate(state.savedRoot, state.formDef);
    }
}
