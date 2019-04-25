package org.javarosa.core.benchmark;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.test.FormParseInit;
import org.javarosa.core.util.PathConst;
import org.javarosa.form.api.FormEntryController;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PopulateTreeNodeBenchmark {

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
    benchmark_TreeElement_populate(TreeElementPopulateState state, Blackhole bh) throws IOException {
        state.dataRootNode.populate(state.savedRoot, state.formDef);
    }

    // The JMH samples are the best documentation for how to use it
    // http://hg.openjdk.java.net/code-tools/jmh/file/tip/jmh-samples/src/main/java/org/openjdk/jmh/samples/
    @State(Scope.Thread)
    public static class TreeElementPopulateState {
        private TreeElement dataRootNode;
        private TreeElement savedRoot;
        private FormDef formDef;

        @Setup(Level.Trial)
        public void
        initialize() throws IOException {
            FormParseInit formParseInit = new FormParseInit(BenchmarkUtils.getNigeriaWardsXMLWithInternal2ndryInstance().toPath());
            FormEntryController formEntryController = formParseInit.getFormEntryController();
            byte[] formInstanceAsBytes;
            formInstanceAsBytes = Files.readAllBytes(Paths.get(PathConst.getTestResourcePath().getAbsolutePath(), "populate-nodes-attributes-instance.xml"));

            savedRoot = XFormParser.restoreDataModel(formInstanceAsBytes, null).getRoot();
            formDef = formEntryController.getModel().getForm();
            dataRootNode = formDef.getInstance().getRoot().deepCopy(true);
        }
    }

}
