package org.javarosa.core.benchmark;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.reference.ReferenceManagerTestUtils;
import org.javarosa.xform.parse.FormParserHelper;
import org.javarosa.xform.util.XFormUtils;
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class FormParserHelperParseExternalSecInstanceBenchmark {

    @Test
    public void
    launchBenchmark() throws Exception {
        try{
            RunResult run = new Runner(BenchmarkUtils.getJVMOptions(this.getClass().getName())).run().iterator().next();

        }catch (NoBenchmarksException nbe){

        }
    }
    @State(Scope.Thread)
    public static class FormParserHelperParseExternalSecondaryInstanceState {
        Path xFormFilePath = BenchmarkUtils.getNigeriaWardsXMLWithExternal2ndryInstance().toPath();
        InputStream xFormFileInputStream;
        @Setup(Level.Trial)
        public void
        initialize() throws FileNotFoundException {
            ReferenceManagerTestUtils.setUpSimpleReferenceManager("file", xFormFilePath.getParent());
            xFormFileInputStream = new FileInputStream(xFormFilePath.toString());
        }
    }

    @Benchmark
    public void
    benchmark_FormParserHelper_parse_external_secondary_instance_filer(FormParserHelperParseExternalSecondaryInstanceState state, Blackhole bh) throws IOException {
        FormDef parsedXFormToFormDef = FormParserHelper.parse(state.xFormFilePath);
        bh.consume(parsedXFormToFormDef);
    }

    @Benchmark
    public void
    benchmark_FormParserHelper_parse_external_secondary_instance_inputstream(FormParserHelperParseExternalSecondaryInstanceState state, Blackhole bh) throws IOException {
        FormDef parsedXFormToFormDef = XFormUtils.getFormFromInputStream(state.xFormFileInputStream);
        bh.consume(parsedXFormToFormDef);
    }


}
