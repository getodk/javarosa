package org.javarosa.core.benchmark;

import org.javarosa.core.model.instance.ExternalDataInstance;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManagerTestUtils;
import org.javarosa.core.util.PathConst;
import org.javarosa.xml.util.InvalidStructureException;
import org.javarosa.xml.util.UnfullfilledRequirementsException;
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
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.nio.file.Path;

public class ExternalDataInstanceBuildBenchmark {

    @Test
    public void
    launchBenchmark() throws Exception {
        try{
            RunResult run = new Runner(BenchmarkUtils.getJVMOptions(this.getClass().getName())).run().iterator().next();

        }catch (NoBenchmarksException nbe){

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
    benchmark_ExternalDataInstance_build(ExternalDataInstanceState state, Blackhole bh)
        throws IOException, XmlPullParserException, InvalidReferenceException,
        UnfullfilledRequirementsException, InvalidStructureException {
        bh.consume(ExternalDataInstance.build("jr://file/wards.xml", "wards"));
    }

}
