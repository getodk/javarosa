package org.javarosa.core.model;

/** Form complexity metrics for performance analysis. Not part of the JavaRosa API. */
public class FormComplexityMetrics {
    public final int numTriggerables;

    public FormComplexityMetrics(int numTriggerables) {
        this.numTriggerables = numTriggerables;
    }
}
