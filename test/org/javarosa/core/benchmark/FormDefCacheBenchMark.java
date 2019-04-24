package org.javarosa.core.benchmark;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.test.FormDefCache;
import org.javarosa.core.reference.ReferenceManagerTestUtils;
import org.javarosa.core.test.FormParseInit;
import org.javarosa.core.util.PathConst;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.xform.parse.FormParserHelper;
import org.javarosa.xform.parse.XFormParser;
import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.NoBenchmarksException;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class FormDefCacheBenchMark {

    // The JMH samples are the best documentation for how to use it
    // http://hg.openjdk.java.net/code-tools/jmh/file/tip/jmh-samples/src/main/java/org/openjdk/jmh/samples/
    @State(Scope.Thread)
    public static class FormDefCacheState {
        Path resourcePath = r("nigeria_wards_external.xml");
        FormDef formDef;
        File formDefCache = new File( resourcePath.toFile().getParentFile().getAbsolutePath() + File.separator +(System.currentTimeMillis() + ".formdef"));

        @Setup(Level.Trial)
        public void
        initialize() throws IOException {
            ReferenceManagerTestUtils.setUpSimpleReferenceManager("file", resourcePath.getParent());
            formDef = FormParserHelper.parse(resourcePath);
            formDefCache.createNewFile();
        }
    }

    @Test
    public void
    launchBenchmark() throws RunnerException {
        /**
         * JMH tests throw this Exception when run with gradle build
         */
        try{
            RunResult run = new Runner(BenchmarkUtils.getJVMOptions(this.getClass().getName())).run().iterator().next();

        }catch (NoBenchmarksException nbe){

        }

    }

    @Benchmark
    public void
    benchmark_FormDefCache_writeToCache(FormDefCacheState state, Blackhole bh) throws IOException {
        FormDefCache.writeCache(state.formDef, state.formDefCache.getPath());
    }


}
