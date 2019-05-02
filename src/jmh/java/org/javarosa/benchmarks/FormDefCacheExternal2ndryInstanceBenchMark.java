package org.javarosa.benchmarks;

import org.javarosa.benchmarks.utils.FormDefCache;
import org.javarosa.core.model.CoreModelModule;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.reference.ReferenceManagerTestUtils;
import org.javarosa.core.services.PrototypeManager;
import org.javarosa.core.util.JavaRosaCoreModule;
import org.javarosa.model.xform.XFormsModule;
import org.javarosa.xform.parse.FormParserHelper;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.nio.file.Path;

public class FormDefCacheExternal2ndryInstanceBenchMark {

    // The JMH samples are the best documentation for how to use it
    // http://hg.openjdk.java.net/code-tools/jmh/file/tip/jmh-samples/src/main/java/org/openjdk/jmh/samples/
    @State(Scope.Thread)
    public static class FormDefCacheState {
        Path resourcePath = BenchmarkUtils.getNigeriaWardsXMLWithInternal2ndryInstance();
        FormDef formDef;
        @Setup(Level.Trial)
        public void
        initialize() throws IOException {

            ReferenceManagerTestUtils.setUpSimpleReferenceManager("file", resourcePath.getParent());
            formDef = FormParserHelper.parse(resourcePath);
            PrototypeManager.registerPrototypes(JavaRosaCoreModule.classNames);
            PrototypeManager.registerPrototypes(CoreModelModule.classNames);
            new XFormsModule().registerModule();
        }
    }

    @Benchmark
    public void
    benchmark_FormDefCache_1_writeToCache(FormDefCacheState state, Blackhole bh) throws IOException {
        FormDefCache.writeCache(state.formDef, state.resourcePath.toString());
    }

    @Benchmark
    public void
    benchmark_FormDefCache_2_readFromCache(FormDefCacheState state, Blackhole bh) throws IOException {
        FormDef cachedFormDef = FormDefCache.readCache(state.resourcePath.toFile());
        bh.consume(cachedFormDef);
    }


}
