package org.javarosa.core.model;

import java.util.Comparator;

import org.javarosa.core.model.condition.Condition;
import org.javarosa.core.model.condition.Recalculate;
import org.javarosa.core.model.condition.Triggerable;

/**
 * Triggerable implementations have a deep equals() comparison operator. Once
 * the DAG is built, we only need a shallow compare since we are not creating
 * any new Triggerable objects.
 *
 * This class serves that purpose.
 *
 * @author mitchellsundt@gmail.com
 *
 */
public final class QuickTriggerable {

    public final Triggerable t;
    private Integer hashCode = null;

    public static final Comparator<Triggerable> triggerablesRootOrdering = new Comparator<Triggerable>() {
        @Override
        public int compare(Triggerable lhs, Triggerable rhs) {
            int cmp;
            cmp = lhs.getContext().toString(false).compareTo(rhs.getContext().toString(false));
            if (cmp != 0) {
                return cmp;
            }
            cmp = lhs.getOriginalContext().toString(false).compareTo(rhs.getOriginalContext().toString(false));
            if (cmp != 0) {
                return cmp;
            }

            // bias toward cascading targets....
            if (lhs.isCascadingToChildren()) {
                if (!rhs.isCascadingToChildren()) {
                    return -1;
                }
            } else if (rhs.isCascadingToChildren()) {
                return 1;
            }

            int lhsHash = lhs.hashCode();
            int rhsHash = rhs.hashCode();
            return (lhsHash < rhsHash) ? -1 : ((lhsHash == rhsHash) ? 0 : 1);
        }
    };

    public static Comparator<QuickTriggerable> quickTriggerablesRootOrdering = new Comparator<QuickTriggerable>() {

        @Override
        public int compare(QuickTriggerable lhs, QuickTriggerable rhs) {
            Triggerable lhst = lhs.t;
            Triggerable rhst = rhs.t;
            int cmp = triggerablesRootOrdering.compare(lhst, rhst);
            return cmp;
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
