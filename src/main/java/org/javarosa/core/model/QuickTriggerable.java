package org.javarosa.core.model;

import java.util.Comparator;
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

    public static Comparator<QuickTriggerable> quickTriggerablesRootOrdering = new Comparator<QuickTriggerable>() {
        @Override
        public int compare(QuickTriggerable lhs, QuickTriggerable rhs) {
            int cmp;
            // TODO Study if there's a better way to compare refs other than using their string representation
            cmp = lhs.t.getContext().toString(false).compareTo(rhs.t.getContext().toString(false));
            if (cmp != 0) {
                return cmp;
            }
            // TODO Study if we ever need this, since the origintal context ref should always equal the context ref.
            cmp = lhs.t.getOriginalContext().toString(false).compareTo(rhs.t.getOriginalContext().toString(false));
            if (cmp != 0) {
                return cmp;
            }

            // bias toward cascading targets....
            if (lhs.t.isCascadingToChildren()) {
                if (!rhs.t.isCascadingToChildren()) {
                    return -1;
                }
            } else if (rhs.t.isCascadingToChildren()) {
                return 1;
            }

            int lhsHash = lhs.t.hashCode();
            int rhsHash = rhs.t.hashCode();
            return (lhsHash < rhsHash) ? -1 : ((lhsHash == rhsHash) ? 0 : 1);
        }
    };

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
