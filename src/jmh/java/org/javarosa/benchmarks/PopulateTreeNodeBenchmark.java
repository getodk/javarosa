package org.javarosa.benchmarks;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.test.FormParseInit;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.xform.parse.XFormParser;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class PopulateTreeNodeBenchmark {
    public static void main(String[] args) {
        BenchmarkUtils.dryRun(PopulateTreeNodeBenchmark.class);
    }

   @Benchmark
    public void benchmarkPopulate(TreeElementPopulateState state) {
        state.dataRootNode.populate(state.savedRoot, state.formDef);
    }

    @State(Scope.Thread)
    public static class TreeElementPopulateState {
        private TreeElement dataRootNode;
        private TreeElement savedRoot;
        private FormDef formDef;

        @Setup(Level.Trial)
        public void initialize() throws IOException {
             Path formFile = BenchmarkUtils.getNigeriaWardsXMLWithInternal2ndryInstance();
            Path submissionFile = BenchmarkUtils.getSubmissionFile();
            FormParseInit formParseInit = new FormParseInit(formFile);
            FormEntryController formEntryController = formParseInit.getFormEntryController();
            byte[] formInstanceAsBytes = Files.readAllBytes(submissionFile);
            savedRoot = XFormParser.restoreDataModel(formInstanceAsBytes, null).getRoot();
            formDef = formEntryController.getModel().getForm();
            dataRootNode = formDef.getInstance().getRoot().deepCopy(true);
        }
    }
}
