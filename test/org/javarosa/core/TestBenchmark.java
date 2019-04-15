package org.javarosa.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.ExternalDataInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.core.reference.ReferenceManagerTestUtils;
import org.javarosa.core.test.FormParseInit;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.xform.parse.FormParserHelper;
import org.javarosa.xform.parse.XFormParser;
import org.javarosa.xml.util.InvalidStructureException;
import org.javarosa.xml.util.UnfullfilledRequirementsException;
import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParserException;


import static org.javarosa.core.reference.ReferenceManagerTestUtils.buildReferenceFactory;
import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.junit.Assert.fail;

public class TestBenchmark {
    private static final Logger logger = LoggerFactory.getLogger(TestBenchmark.class);
    @Test
    public void
    launchBenchmark() throws Exception {
        Options opt = new OptionsBuilder()
            // Specify which benchmarks to run.
            // You can be more specific if you'd like to run only one benchmark per test.
            .include(this.getClass().getName() + ".*")
            // Set the following options as needed
            .mode(Mode.AverageTime)
            .timeUnit(TimeUnit.SECONDS)
            .warmupTime(TimeValue.seconds(5))
            .warmupIterations(5)
            .measurementTime(TimeValue.seconds(1))
            .threads(1)
            .measurementIterations(5)
            .forks(1)
            .shouldFailOnError(true)
            .shouldDoGC(true)
            //.jvmArgs("-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintInlining")
            //.jvmArgs("-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-XX:MaxRAMFraction=10")
            //.addProfiler(WinPerfAsmProfiler.class)
            .build();

        RunResult run = new Runner(opt).run().iterator().next();
        //run.
    }


    // The JMH samples are the best documentation for how to use it
    // http://hg.openjdk.java.net/code-tools/jmh/file/tip/jmh-samples/src/main/java/org/openjdk/jmh/samples/
    @State(Scope.Thread)
    public static class BenchmarkState {
        private TreeElement dataRootNode;
        private TreeElement savedRoot;
        private FormDef formDef;

        @Setup(Level.Trial)
        public void
        initialize() {

            FormParseInit formParseInit = new FormParseInit(r("nigeria_wards_external_combined.xml"));

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

    @State(Scope.Thread)
    public static class ParseWithExternalInstanceState {
        Path xFormFilePath = r("nigeria_wards_external.xml");
        Path resourcePath = xFormFilePath.getParent();
        FormDef formDef;
        @Setup(Level.Trial)
        public void
        initialize() {
            Path resourcePath = PathConst.getTestResourcePath().toPath();
            ReferenceManagerTestUtils.setUpSimpleReferenceManager("file", resourcePath);
        }
    }


    @State(Scope.Thread)
    public static class ParseWithInternalInstanceState {
        Path xFormFilePath = r("nigeria_wards_external_combined.xml");
        Path resourcePath = xFormFilePath.getParent();
        FormDef formDef;
        @Setup(Level.Trial)
        public void
        initialize() {
            Path resourcePath = PathConst.getTestResourcePath().toPath();
            ReferenceManagerTestUtils.setUpSimpleReferenceManager("file", resourcePath);

        }
    }

    @State(Scope.Thread)
    public static class ExternalDataInstanceState {
        ExternalDataInstance externalDataInstance = null;

        @Setup(Level.Trial)
        public void
        initialize() {
            Path resourcePath = PathConst.getTestResourcePath().toPath();
            ReferenceManagerTestUtils.setUpSimpleReferenceManager("file", resourcePath);

        }
    }


    @Benchmark
    public void
    benchmarkPopulateRootNode(BenchmarkState state, Blackhole bh) {
        state.dataRootNode.populate(state.savedRoot, state.formDef);

  }

    @Benchmark
    public void
    benchmarkParseXFormWithExternalInstances(ParseWithExternalInstanceState state, Blackhole bh) {
        try {
            bh.consume(FormParserHelper.parse(state.xFormFilePath));
        } catch (IOException e) {
            fail("There was a problem with reading the test data.\n" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Benchmark
    public void
    benchmarkParseXFormWithInternalInstances(ParseWithInternalInstanceState state, Blackhole bh) {
        try {
            bh.consume(FormParserHelper.parse(state.xFormFilePath));
        } catch (IOException e) {
            fail("There was a problem with reading the test data.\n" + e.getMessage());
            throw new RuntimeException(e);
        }
    }


    @Benchmark
    public void
    benchmarkParseExternalInstances(ExternalDataInstanceState state, Blackhole bh) {
        try {
            bh.consume(ExternalDataInstance.build("jr://file/wards.xml", "wards"));
        } catch (IOException e) {
            fail("There was a problem with reading the test data.\n" + e.getMessage());
            throw new RuntimeException(e);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (InvalidReferenceException e) {
            e.printStackTrace();
        } catch (UnfullfilledRequirementsException e) {
            e.printStackTrace();
        } catch (InvalidStructureException e) {
            e.printStackTrace();
        }
    }


}
