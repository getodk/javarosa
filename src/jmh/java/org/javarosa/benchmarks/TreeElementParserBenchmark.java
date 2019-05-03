package org.javarosa.benchmarks;

import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.XmlXFormInstance;
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

public class TreeElementParserBenchmark {
    public static void main(String[] args) {
        dryRun(TreeElementParserBenchmark.class);
    }

    @State(Scope.Thread)
    public static class XmlExternalInstanceState {
        String xFormWithOnlyInternalSecondaryInstances;
        String xFormWithInternalAndExternalSecondayInstances;
        String lgaExternalSecondaryInstance;
        String wardExternalSecondaryInstance;
        @Setup(Level.Trial)
        public void initialize() {
            xFormWithOnlyInternalSecondaryInstances = BenchmarkUtils.getNigeriaWardsXMLWithInternal2ndryInstance().toString();
            xFormWithInternalAndExternalSecondayInstances = BenchmarkUtils.getNigeriaWardsXMLWithExternal2ndryInstance().toString();
            lgaExternalSecondaryInstance = BenchmarkUtils.getLGAsExternalInstance().toString();
            wardExternalSecondaryInstance = BenchmarkUtils.getWardsExternalInstance().toString();
        }
    }

    @Benchmark
    public void benchmark_TreeElementParser_parse_internal_secondary_instance(XmlExternalInstanceState state, Blackhole bh) throws IOException, UnfullfilledRequirementsException, XmlPullParserException, InvalidStructureException {
        TreeElement documentRootTreeElement = XmlXFormInstance.parse("nigeria-wards", state.xFormWithOnlyInternalSecondaryInstances);
        bh.consume(documentRootTreeElement);
    }


    @Benchmark
    public void benchmark_TreeElementParser_parse_external_secondary_instance(XmlExternalInstanceState state, Blackhole bh) throws IOException, UnfullfilledRequirementsException, XmlPullParserException, InvalidStructureException {
        TreeElement xFormTreeElement = XmlXFormInstance.parse("nigeria-wards", state.xFormWithInternalAndExternalSecondayInstances);
        TreeElement lgaInstanceTreeElement = XmlXFormInstance.parse("lgas", state.lgaExternalSecondaryInstance);
        TreeElement wardInstanceTreeElement = XmlXFormInstance.parse("wards", state.wardExternalSecondaryInstance);
        bh.consume(xFormTreeElement);
        bh.consume(lgaInstanceTreeElement);
        bh.consume(wardInstanceTreeElement);
    }
}
