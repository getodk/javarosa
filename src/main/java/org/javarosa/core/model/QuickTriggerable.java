package org.javarosa.core.model;

import org.javarosa.core.model.condition.Condition;
import org.javarosa.core.model.condition.Recalculate;
import org.javarosa.core.model.condition.Triggerable;

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

    public final Triggerable t;
    private Integer hashCode = null;

    @Override
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
}
