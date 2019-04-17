package org.javarosa.core.benchmark;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.reference.ReferenceManagerTestUtils;
import org.javarosa.core.util.PathConst;
import org.javarosa.xform.parse.FormParserHelper;
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

import java.io.IOException;
import java.nio.file.Path;

import static org.javarosa.test.utils.ResourcePathHelper.r;

public class FormDefValidateBenchmark {

    @Test
    public void
    launchBenchmark() throws Exception {
        try{
            RunResult run = new Runner(BenchmarkUtils.getJVMOptions(this.getClass().getName())).run().iterator().next();

        }catch (NoBenchmarksException nbe){

        }
    }

    @State(Scope.Thread)
    public static class FormDefValidateState {
        FormDef formDef = null;
        @Setup(Level.Trial)
        public void
        initialize() throws IOException {
            Path resourcePath = r("nigeria_wards_external_combined.xml");
            ReferenceManagerTestUtils.setUpSimpleReferenceManager("file", PathConst.getTestResourcePath().toPath());
            formDef = FormParserHelper.parse(resourcePath);
        }
    }

    @Benchmark
    public void
    benchmark_FormDefValidate_validate(FormDefValidateState state, Blackhole bh) {
        bh.consume(state.formDef.validate(true));
    }

}
