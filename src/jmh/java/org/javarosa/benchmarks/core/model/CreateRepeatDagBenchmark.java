package org.javarosa.benchmarks.core.model;

import static java.util.stream.IntStream.range;
import static org.javarosa.benchmarks.BenchmarkUtils.dryRun;
import static org.javarosa.core.util.BindBuilderXFormsElement.bind;
import static org.javarosa.core.util.XFormsElement.body;
import static org.javarosa.core.util.XFormsElement.head;
import static org.javarosa.core.util.XFormsElement.html;
import static org.javarosa.core.util.XFormsElement.input;
import static org.javarosa.core.util.XFormsElement.mainInstance;
import static org.javarosa.core.util.XFormsElement.model;
import static org.javarosa.core.util.XFormsElement.repeat;
import static org.javarosa.core.util.XFormsElement.t;
import static org.javarosa.core.util.XFormsElement.title;

import java.io.IOException;
import org.javarosa.core.test.Scenario;
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
public class CreateRepeatDagBenchmark {
    public static void main(String[] args) {
        dryRun(CreateRepeatDagBenchmark.class);
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
        public void setUp() throws IOException {
            expressionInsideScenario = getExpressionInsideScenario();
            expressionInsideWithRefOutsideScenario = getExpressionInsideWithRefOutsideScenario();
            sumExpressionOutsideScenario = getSumExpressionOutsideScenario();

            expressionInsideWithPositionCallScenario = getExpressionInsideWithPositionCallScenario();
        }
    }

    @Benchmark
    // Expect throughput to be linearly related to the number of repeats because expressions should only be evaluated on the new instance.
    public void createRepeat_withExpressionReferencingSibling(ExecutionPlan plan, Blackhole bh) {
        range(0, plan.repeatCount).forEach(n -> {
            bh.consume(plan.expressionInsideScenario.next());
            bh.consume(plan.expressionInsideScenario.createNewRepeat());
            bh.consume(plan.expressionInsideScenario.next());
        });
    }

    @Benchmark
    // Expect results to be identical to the case where a sibling is referenced.
    public void createRepeat_withExpressionReferencingOuterNode(ExecutionPlan plan, Blackhole bh) {
        range(0, plan.repeatCount).forEach(n -> {
            bh.consume(plan.expressionInsideWithRefOutsideScenario.next());
            bh.consume(plan.expressionInsideWithRefOutsideScenario.createNewRepeat());
            bh.consume(plan.expressionInsideWithRefOutsideScenario.next());
        });
    }

    @Benchmark
    // Expect results to be identical to the case where a sibling is referenced.
    public void createRepeat_withSumExpressionOutside(ExecutionPlan plan, Blackhole bh) {
        range(0, plan.repeatCount).forEach(n -> {
            bh.consume(plan.sumExpressionOutsideScenario.next());
            bh.consume(plan.sumExpressionOutsideScenario.createNewRepeat());
            bh.consume(plan.sumExpressionOutsideScenario.next());
        });
    }

    @Benchmark
    // Expect throughput to go down as repeat instance count goes up because position is recomputed for every instance.
    public void createRepeat_withPositionExpression(ExecutionPlan plan, Blackhole bh) {
        range(0, plan.repeatCount).forEach(n -> {
            bh.consume(plan.expressionInsideWithPositionCallScenario.next());
            bh.consume(plan.expressionInsideWithPositionCallScenario.createNewRepeat());
            bh.consume(plan.expressionInsideWithPositionCallScenario.next());
        });
    }

    private static Scenario getExpressionInsideScenario() throws IOException {
        return Scenario.init("Repeat with expression inside", html(
            head(
                title("Repeat with expression inside"),
                model(
                    mainInstance(t("data id=\"sibling-expression\"",
                        t("random"),

                        t("repeat jr:template=\"\"",
                            t("random"),
                            t("random_x2")
                        ))),
                    bind("/data/random").type("int").calculate("random()"),
                    bind("/data/repeat/random").type("int").calculate("random()"),
                    bind("/data/repeat/random_x2").type("int").calculate("../random * 2"))),
            body(
                repeat("/data/repeat",
                    input("/data/repeat/random")
                )
            )));
    }

    private static Scenario getExpressionInsideWithRefOutsideScenario() throws IOException {
        return Scenario.init("Repeat with expression inside referencing outside", html(
            head(
                title("Repeat with expression inside referencing outside"),
                model(
                    mainInstance(t("data id=\"outside-ref\"",
                        t("random"),

                        t("repeat jr:template=\"\"",
                            t("random"),
                            t("random_outer")
                        ))),
                    bind("/data/random").type("int").calculate("random()"),
                    bind("/data/repeat/random").type("int").calculate("random()"),
                    bind("/data/repeat/random_outer").type("int").calculate("/data/random"))),
            body(
                repeat("/data/repeat",
                    input("/data/repeat/random")
                )
            )));
    }

    private static Scenario getSumExpressionOutsideScenario() throws IOException {
        return Scenario.init("Repeat with sum expression outside", html(
            head(
                title("Repeat with sum expression outside"),
                model(
                    mainInstance(t("data id=\"outside-sum\"",
                        t("sum"),

                        t("repeat jr:template=\"\"",
                            t("random"),
                            t("random_outer")
                        ))),
                    bind("/data/sum").type("int").calculate("sum(/data/repeat/random)"),
                    bind("/data/repeat/random").type("int").calculate("random()"),
                    bind("/data/repeat/random_x2").type("int").calculate("/data/repeat/random * 2"))),
            body(
                repeat("/data/repeat",
                    input("/data/repeat/random")
                )
            )));
    }

    private static Scenario getExpressionInsideWithPositionCallScenario() throws IOException {
        return Scenario.init("Repeat with expression inside referencing outside", html(
            head(
                title("Repeat with expression inside referencing outside"),
                model(
                    mainInstance(t("data id=\"outside-ref\"",
                        t("random"),

                        t("repeat jr:template=\"\"",
                            t("random"),
                            t("position")
                        ))),
                    bind("/data/random").type("int").calculate("random()"),
                    bind("/data/repeat/random").type("int").calculate("random()"),
                    bind("/data/repeat/position").type("int").calculate("position(..)"))),
            body(
                repeat("/data/repeat",
                    input("/data/repeat/random")
                )
            )));
    }
}
