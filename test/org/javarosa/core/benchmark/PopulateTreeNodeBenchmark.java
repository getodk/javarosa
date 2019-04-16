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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.junit.Assert.fail;

public class PopulateTreeNodeBenchmark {
    private static final Logger logger = LoggerFactory.getLogger(FormParserHelperParseESIBenchmark.class);

    @Test
    public void
    launchBenchmark() throws Exception {
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
        initialize() {
            FormParseInit formParseInit = new FormParseInit(r("nigeria_wards_external_combined.xml"));
            FormEntryController formEntryController = formParseInit.getFormEntryController();
            byte[] formInstanceAsBytes = null;
            try {
                formInstanceAsBytes = Files.readAllBytes(Paths.get(PathConst.getTestResourcePath().getAbsolutePath(), "populate-nodes-attributes-instance.xml"));
            } catch (IOException e) {
                fail("There was a problem with reading the test data.\n" + e.getMessage());
            }
            savedRoot = XFormParser.restoreDataModel(formInstanceAsBytes, null).getRoot();
            formDef = formEntryController.getModel().getForm();
            dataRootNode = formDef.getInstance().getRoot().deepCopy(true);
        }
    }

}
