package org.javarosa.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.test.FormParseInit;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.xform.parse.IXFormParserFactory;
import org.javarosa.xform.parse.XFormParseException;
import org.javarosa.xform.parse.XFormParser;
import org.javarosa.xform.parse.XFormParserFactory;
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
            .timeUnit(TimeUnit.MICROSECONDS)
            .warmupTime(TimeValue.seconds(1))
            .warmupIterations(2)
            .measurementTime(TimeValue.seconds(1))
            .measurementIterations(2)
            .threads(2)
            .measurementIterations(5)
            .forks(1)
            .shouldFailOnError(true)
            .shouldDoGC(true)
            //.jvmArgs("-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintInlining")
            //.addProfiler(WinPerfAsmProfiler.class)
            .build();

        RunResult run = new Runner(opt).run().iterator().next();
        //run.
    }

    // The JMH samples are the best documentation for how to use it
    // http://hg.openjdk.java.net/code-tools/jmh/file/tip/jmh-samples/src/main/java/org/openjdk/jmh/samples/
    @State(Scope.Thread)
    public static class BenchmarkState {
        List<Integer> list;

        @Setup(Level.Trial)
        public void
        initialize() {

            Random rand = new Random();

            list = new ArrayList<>();
            for (int i = 0; i < 1000; i++)
                list.add(rand.nextInt());
        }
    }

    @Benchmark
    public void
    benchmark1(BenchmarkState state, Blackhole bh) {

        List<Integer> list = state.list;

        for (int i = 0; i < 1000; i++)
            bh.consume(list.get(i));
    }

    @Benchmark
    public void
    benchmark2(BenchmarkState state, Blackhole bh) {
        bh.consume(Benchmarks.benchMark2());

  }

    @Benchmark
    public void
    benchmark3(BenchmarkState state, Blackhole bh) {
         Benchmarks.benchMark2();

//        String fileName = "populate-nodes-attributes-instance.xml";
//        Path filePath = r(fileName);
//        InputStreamReader isr = null;
//
//        InputStream is =  Benchmarks.getFileInputStream(filePath.toAbsolutePath().toString());
//        try {
//            try {
//                isr = new InputStreamReader(is, "UTF-8");
//            } catch (UnsupportedEncodingException uee) {
//                throw new XFormParseException("IO Exception during parse! " + uee.getMessage());
//            }
//
//            XFormParser xFormParser = Benchmarks.getXFormParserFactory().getXFormParser(isr);
//            bh.consume(xFormParser.parse("");
//        } catch(IOException e) {
//
//            e.printStackTrace();
//            throw new XFormParseException("IO Exception during parse! " + e.getMessage());
//        } finally {
//            try {
//                if (isr != null) {
//                    isr.close();
//                }
//            } catch (IOException e) {
//                logger.error("IO Exception while closing stream.", e);
//            }
//        }
    }
}
