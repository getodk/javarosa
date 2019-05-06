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
    public static class TreeElementParserState {
        String xFormMinifiedInternalSecondaryInstances;
        String xFormInternalSecondaryInstances;
        String xFormExternalSecondayInstances;
        String lgasInstance;
        String wardsInstance;
        @Setup(Level.Trial)
        public void initialize() {
            xFormMinifiedInternalSecondaryInstances = BenchmarkUtils.getMinifiedNigeriaWardsXMLWithInternal2ndryInstance().toString();
            xFormInternalSecondaryInstances = BenchmarkUtils.getNigeriaWardsXMLWithInternal2ndryInstance().toString();
            xFormExternalSecondayInstances = BenchmarkUtils.getNigeriaWardsXMLWithExternal2ndryInstance().toString();
            lgasInstance = BenchmarkUtils.getLGAsExternalInstance().toString();
            wardsInstance = BenchmarkUtils.getWardsExternalInstance().toString();
        }
    }

    @Benchmark
    public void benchmarkParseMinifiedInternalInstanceXForm(TreeElementParserState state, Blackhole bh) throws IOException, UnfullfilledRequirementsException, XmlPullParserException, InvalidStructureException {
        TreeElement documentRootTreeElement = XmlXFormInstance.parse("nigeria-wards", state.xFormMinifiedInternalSecondaryInstances);
        bh.consume(documentRootTreeElement);
    }

    @Benchmark
    public void benchmarkParseExternalInstanceXFormOnly(TreeElementParserState state, Blackhole bh) throws IOException, UnfullfilledRequirementsException, XmlPullParserException, InvalidStructureException {
        TreeElement documentRootTreeElement = XmlXFormInstance.parse("nigeria-wards", state.xFormExternalSecondayInstances);
        bh.consume(documentRootTreeElement);
    }

    @Benchmark
    public void benchmarkParseInternalInstanceXForm(TreeElementParserState state, Blackhole bh) throws IOException, UnfullfilledRequirementsException, XmlPullParserException, InvalidStructureException {
        TreeElement treeElement = XmlXFormInstance.parse("nigeria-wards", state.xFormInternalSecondaryInstances);
        bh.consume(treeElement);
    }


    @Benchmark
    public void benchmarkParseExternalInstanceXFormWithInstanceFiles(TreeElementParserState state, Blackhole bh) throws IOException, UnfullfilledRequirementsException, XmlPullParserException, InvalidStructureException {
        TreeElement xFormTreeElement = XmlXFormInstance.parse("nigeria-wards", state.xFormExternalSecondayInstances);
        TreeElement lgaInstanceTreeElement = XmlXFormInstance.parse("lgas", state.lgasInstance);
        TreeElement wardInstanceTreeElement = XmlXFormInstance.parse("wards", state.wardsInstance);
        bh.consume(xFormTreeElement);
        bh.consume(lgaInstanceTreeElement);
        bh.consume(wardInstanceTreeElement);
    }
}
