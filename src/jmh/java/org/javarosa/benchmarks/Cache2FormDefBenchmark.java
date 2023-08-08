package org.javarosa.benchmarks;

import org.javarosa.benchmarks.utils.FormDefCache;
import org.javarosa.core.model.FormDef;
import org.javarosa.xform.parse.FormParserHelper;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.io.File;
import java.io.IOException;

import static org.javarosa.core.reference.ReferenceManagerTestUtils.setUpSimpleReferenceManager;
import static org.javarosa.benchmarks.BenchmarkUtils.dryRun;
import static org.javarosa.benchmarks.BenchmarkUtils.getCachePath;
import static org.javarosa.benchmarks.BenchmarkUtils.getWorkingDir;
import static org.javarosa.benchmarks.BenchmarkUtils.registerCacheProtoTypes;

public class Cache2FormDefBenchmark {

    public static void main(String[] args) {
        dryRun(Cache2FormDefBenchmark.class);
    }

    @State(Scope.Thread)
    public static class FormTypesState {
        File xFormXmlFile ;
        FormDef formDef ;
        String CACHE_PATH;
        @Param({"10", "200", "500"})
        public int noOfQuestions;
        @Param({"1", "10"})
        public int noOfInternalSecondaryInstances;
        @Param({"50", "500", "5000"})
        public int noOf2ndryInstanceElements;
        @Param({"0"})
        public int noOfQuestionGroups;
        @Param({"0"})
        public int noOfExternalSecondaryInstances;
        @Setup(Level.Trial)
        public void initialize() throws IOException {
            CACHE_PATH = getCachePath().toString();
            xFormXmlFile = BenchmarkUtils.generateXFormFile(noOfQuestions, noOfQuestionGroups, noOfInternalSecondaryInstances, noOfExternalSecondaryInstances, noOf2ndryInstanceElements);
            setUpSimpleReferenceManager(getWorkingDir(),"file");
            String formPath = xFormXmlFile.getPath();
            formDef =  FormParserHelper.parse(xFormXmlFile.toPath());
            registerCacheProtoTypes();
            FormDefCache.writeCache(formDef, formPath, CACHE_PATH);
        }
    }

    @Benchmark
    public void runBenchmark(FormTypesState state, Blackhole bh) throws IOException {
        FormDef formDef = FormDefCache.readCache(state.xFormXmlFile, state.CACHE_PATH);
        bh.consume(formDef);
    }

}
