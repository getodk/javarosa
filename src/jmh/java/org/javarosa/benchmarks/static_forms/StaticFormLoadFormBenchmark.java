package org.javarosa.benchmarks.static_forms;

import org.javarosa.benchmarks.BenchmarkUtils;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.InstanceInitializationFactory;
import org.javarosa.xform.parse.FormParserHelper;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.nio.file.Path;

import static org.javarosa.benchmarks.BenchmarkUtils.dryRun;

public class StaticFormLoadFormBenchmark {
    public static void main(String[] args) {
        dryRun(StaticFormLoadFormBenchmark.class);
    }

    @State(Scope.Thread)
    public static class FormControllerAnswerQuestionState {

        Path formFile;
        @Setup(Level.Trial)
        public void initialize() throws IOException {
            formFile = BenchmarkUtils.getNigeriaWardsXMLWithExternal2ndryInstance();
        }
    }

    @Benchmark
    public void benchmarkAnswerOne(FormControllerAnswerQuestionState state, Blackhole bh) throws IOException {
        FormDef formDef = FormParserHelper.parse(state.formFile);
        formDef.initialize(true, new InstanceInitializationFactory());
        bh.consume(formDef);
    }

}
