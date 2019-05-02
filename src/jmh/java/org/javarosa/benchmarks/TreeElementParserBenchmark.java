package org.javarosa.benchmarks;

import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.XmlXFormInstance;
import org.javarosa.xml.util.InvalidStructureException;
import org.javarosa.xml.util.UnfullfilledRequirementsException;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import static org.javarosa.benchmarks.BenchmarkUtils.dryRun;
import static org.javarosa.benchmarks.BenchmarkUtils.getNigeriaWardsXMLWithInternal2ndryInstance;

public class TreeElementParserBenchmark {
    public static void main(String[] args) {
        dryRun(TreeElementParserBenchmark.class);
    }

    @State(Scope.Thread)
    public static class XmlExternalInstanceState {
        String xFormFilePath;
        @Setup(Level.Trial)
        public void initialize() {

            xFormFilePath = getNigeriaWardsXMLWithInternal2ndryInstance().toString();

        }
    }

    //@Benchmark
    public void benchmark_TreeElementParser_parse_internal_secondary_instance(XmlExternalInstanceState state, Blackhole bh) throws IOException, UnfullfilledRequirementsException, XmlPullParserException, InvalidStructureException {
        TreeElement documentRootTreeElement = XmlXFormInstance.parse("nigeriawards", state.xFormFilePath);
        bh.consume(documentRootTreeElement);
    }
}
