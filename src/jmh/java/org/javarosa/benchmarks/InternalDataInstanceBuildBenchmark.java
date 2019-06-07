package org.javarosa.benchmarks;

import org.javarosa.core.model.instance.InternalDataInstance;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.xml.InternalDataInstanceParser;
import org.javarosa.xml.util.InvalidStructureException;
import org.javarosa.xml.util.UnfullfilledRequirementsException;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import static org.javarosa.benchmarks.BenchmarkUtils.dryRun;

public class InternalDataInstanceBuildBenchmark {
    public static void main(String[] args) {
        dryRun(InternalDataInstanceBuildBenchmark.class);
    }

    @State(Scope.Thread)
    public static class InternalDataInstanceState {
        String xFormInternalSecondaryInstances;
        @Setup(Level.Trial)
        public void initialize() {
            xFormInternalSecondaryInstances = BenchmarkUtils.getNigeriaWardsXMLWithInternal2ndryInstance().toString();
        }
    }

    @Benchmark
    public void benchmarkInternalWardDataInstance(InternalDataInstanceState state, Blackhole bh)
        throws IOException, XmlPullParserException, InvalidReferenceException,
        UnfullfilledRequirementsException, InvalidStructureException {
        InternalDataInstance wardsInternalInstance =
            InternalDataInstanceParser.build(state.xFormInternalSecondaryInstances, "wards");
        bh.consume(wardsInternalInstance);
    }

    @Benchmark
    public void
    benchmarkInternalLGADataInstance(InternalDataInstanceState state, Blackhole bh)
        throws IOException, XmlPullParserException, InvalidReferenceException,
        UnfullfilledRequirementsException, InvalidStructureException {
        InternalDataInstance lgaIInternalInstance =
            InternalDataInstanceParser.build(state.xFormInternalSecondaryInstances, "lgas");
        bh.consume(lgaIInternalInstance);
    }
}
