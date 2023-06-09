package org.javarosa.benchmarks.core.model;

import static java.util.stream.IntStream.range;
import static org.javarosa.benchmarks.BenchmarkUtils.dryRun;
import static org.javarosa.benchmarks.core.model.CreateRepeatDagBenchmark.getExpressionInsideScenario;
import static org.javarosa.benchmarks.core.model.CreateRepeatDagBenchmark.getExpressionInsideWithPositionCallScenario;
import static org.javarosa.benchmarks.core.model.CreateRepeatDagBenchmark.getExpressionInsideWithRefOutsideScenario;
import static org.javarosa.benchmarks.core.model.CreateRepeatDagBenchmark.getSumExpressionOutsideScenario;

import java.io.IOException;
import org.javarosa.core.test.Scenario;
import org.javarosa.xform.parse.ParseException;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 2, time = 1)
public class DeleteRepeatDagBenchmark {
    public static void main(String[] args) {
        dryRun(DeleteRepeatDagBenchmark.class);
    }

    @State(Scope.Thread)
    public static class ExecutionPlan {
        @Param({"250", "500", "1000"})
        int repeatCount;

        Scenario expressionInsideScenario;

        Scenario expressionInsideWithRefOutsideScenario;

        Scenario sumExpressionOutsideScenario;

        Scenario expressionInsideWithPositionCallScenario;

        @Setup(Level.Invocation)
        public void setUp() throws IOException, ParseException {
            expressionInsideScenario = getExpressionInsideScenario();
            range(0, repeatCount).forEach(n -> {
                expressionInsideScenario.next();
                expressionInsideScenario.createNewRepeat();
                expressionInsideScenario.next();
            });

            expressionInsideWithRefOutsideScenario = getExpressionInsideWithRefOutsideScenario();
            range(0, repeatCount).forEach(n -> {
                expressionInsideWithRefOutsideScenario.next();
                expressionInsideWithRefOutsideScenario.createNewRepeat();
                expressionInsideWithRefOutsideScenario.next();
            });

            sumExpressionOutsideScenario = getSumExpressionOutsideScenario();
            range(0, repeatCount).forEach(n -> {
                sumExpressionOutsideScenario.next();
                sumExpressionOutsideScenario.createNewRepeat();
                sumExpressionOutsideScenario.next();
            });

            expressionInsideWithPositionCallScenario = getExpressionInsideWithPositionCallScenario();
            range(0, repeatCount).forEach(n -> {
                expressionInsideWithPositionCallScenario.next();
                expressionInsideWithPositionCallScenario.createNewRepeat();
                expressionInsideWithPositionCallScenario.next();
            });
        }
    }

    @Benchmark
    // Expect throughput to be linearly related to the number of repeats because expressions should only be evaluated on the new instance.
    public void deleteRepeat_withExpressionReferencingSibling(ExecutionPlan plan, Blackhole bh) {
        range(0, plan.repeatCount).forEach(n -> bh.consume(plan.expressionInsideScenario.removeRepeat("/data/repeat[0]")));
    }

    @Benchmark
    // Expect results to be identical to the case where a sibling is referenced.
    public void deleteRepeat_withExpressionReferencingOuterNode(ExecutionPlan plan, Blackhole bh) {
        range(0, plan.repeatCount).forEach(n -> bh.consume(plan.expressionInsideWithRefOutsideScenario.removeRepeat("/data/repeat[0]")));
    }

    @Benchmark
    // Expect throughput to go down as repeat instance count goes up because position is recomputed for every instance.
    public void deleteRepeat_withSumExpressionOutside(ExecutionPlan plan, Blackhole bh) {
        range(0, plan.repeatCount).forEach(n -> bh.consume(plan.sumExpressionOutsideScenario.removeRepeat("/data/repeat[0]")));
    }

    @Benchmark
    // Expect throughput to go down as repeat instance count goes up because position is recomputed for every instance.
    public void deleteRepeat_withPositionExpression(ExecutionPlan plan, Blackhole bh) {
        range(0, plan.repeatCount).forEach(n -> bh.consume(plan.expressionInsideWithPositionCallScenario.removeRepeat("/data/repeat[0]")));
    }
}
