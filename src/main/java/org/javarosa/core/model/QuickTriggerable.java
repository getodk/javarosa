package org.javarosa.core.model;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.javarosa.core.model.condition.Condition;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.Recalculate;
import org.javarosa.core.model.condition.Triggerable;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.debug.EvaluationResult;

/**
 * Triggerable implementations have a deep equals() comparison operator. Once
 * the DAG is built, we only need a shallow compare since we are not creating
 * any new Triggerable objects.
 * <p>
 * This class serves that purpose.
 *
 * @author mitchellsundt@gmail.com
 */
public final class QuickTriggerable {

    private final Triggerable t;
    private Integer hashCode = null;

    public final int hashCode() {
        if (hashCode == null) {
            hashCode = System.identityHashCode(t);
        }
        return hashCode;
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj instanceof QuickTriggerable) {
            QuickTriggerable other = (QuickTriggerable) obj;
            return other.t == t;
        }
        return false;
    }

    QuickTriggerable(Triggerable t) {
        this.t = t;
    }

    public boolean isCondition() {
        return t instanceof Condition;
    }

    public boolean isRecalculate() {
        return t instanceof Recalculate;
    }

    public List<TreeReference> findAffectedTriggers(Map<TreeReference, List<TreeReference>> firedAnchors) {
        return t.findAffectedTriggers(firedAnchors);
    }

    public TreeReference contextualizeContextRef(TreeReference anchorRef) {
        return t.contextualizeContextRef(anchorRef);
    }

    public List<EvaluationResult> apply(FormInstance mainInstance, EvaluationContext ec, TreeReference qualified) {
        return t.apply(mainInstance, ec, qualified);
    }

    public List<TreeReference> getTargets() {
        return t.getTargets();
    }

    public boolean contains(Triggerable t) {
        return this.t.equals(t);
    }

    public Triggerable changeContextRefToIntersectWithTriggerable(Triggerable other) {
        // TODO It's fishy to mutate the Triggerable here. We might prefer to return a copy of the original Triggerable
        t.changeContextRefToIntersectWithTriggerable(other);
        return t;
    }

    public void setImmediateCascades(Set<QuickTriggerable> deps) {
        t.setImmediateCascades(deps);
    }

    public boolean canCascade() {
        return t.canCascade();
    }

    public boolean isCascadingToChildren() {
        return t.isCascadingToChildren();
    }

    public Set<QuickTriggerable> getImmediateCascades() {
        return t.getImmediateCascades();
    }

    // TODO Think how we can avoid breaking encapsulation here
    public Triggerable getTriggerable() {
        return t;
    }

    public Object eval(FormInstance mainInstance, EvaluationContext evaluationContext) {
        return t.eval(mainInstance, evaluationContext);
    }

    public TreeReference getContext() {
        return t.getContext();
    }

    public TreeReference getOriginalContext() {
        return t.getOriginalContext();
    }
}
