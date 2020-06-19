package org.javarosa.core.model;

import java.util.List;
import java.util.Set;
import org.javarosa.core.model.condition.Condition;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.Recalculate;
import org.javarosa.core.model.condition.Triggerable;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.debug.EvaluationResult;

/**
 * This is a thin wrapper class over Triggerable that provides quicker implementation of the equals() method.
 * <p>
 * Triggerable has a deep equals() comparison operator, required while building the DAG. Once the DAG gets
 * built, object references are enough, since no new Triggerables should be created after that.
 */
public final class QuickTriggerable {
    private final Triggerable triggerable;
    private final int hashCode;

    private QuickTriggerable(Triggerable triggerable, int hashCode) {
        this.triggerable = triggerable;
        this.hashCode = hashCode;
    }

    static QuickTriggerable of(Triggerable triggerable) {
        return new QuickTriggerable(triggerable, System.identityHashCode(triggerable));
    }

    public boolean isCondition() {
        return triggerable instanceof Condition;
    }

    boolean isRecalculate() {
        return triggerable instanceof Recalculate;
    }

    public List<EvaluationResult> apply(FormInstance mainInstance, EvaluationContext ec, TreeReference qualified) {
        return triggerable.apply(mainInstance, ec, qualified);
    }

    Set<TreeReference> getTargets() {
        return triggerable.getTargets();
    }

    public boolean contains(Triggerable triggerable) {
        return this.triggerable.equals(triggerable);
    }

    void intersectContextWith(Triggerable other) {
        triggerable.intersectContextWith(other);
    }

    void setImmediateCascades(Set<QuickTriggerable> deps) {
        triggerable.setImmediateCascades(deps);
    }

    public boolean canCascade() {
        return triggerable.canCascade();
    }

    boolean isCascadingToChildren() {
        return triggerable.isCascadingToChildren();
    }

    Set<QuickTriggerable> getImmediateCascades() {
        return triggerable.getImmediateCascades();
    }

    public Triggerable getTriggerable() {
        // TODO Think how we can avoid breaking encapsulation here
        return triggerable;
    }

    public Object eval(FormInstance mainInstance, EvaluationContext evaluationContext) {
        return triggerable.eval(mainInstance, evaluationContext);
    }

    public TreeReference getContext() {
        return triggerable.getContext();
    }

    public TreeReference getOriginalContext() {
        return triggerable.getOriginalContext();
    }

    /**
     * Quicker implementation of Triggerable.equals() that only consider object references.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof QuickTriggerable))
            return false;
        return triggerable == ((QuickTriggerable) obj).triggerable;
    }

    /**
     * Returns the hashCode of the wrapped Triggerable object based on {@link System#identityHashCode(Object)},
     * which should be quicker than the original hashCode method in Triggerable.
     * <p>
     * The actual return value is computed once during object creation at {@link QuickTriggerable#of(Triggerable)}
     */
    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return triggerable.toString();
    }
}
