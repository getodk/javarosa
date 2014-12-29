package org.javarosa.core.model;

import java.util.Comparator;

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

	public static Comparator<QuickTriggerable> quickTriggerablesRootOrdering = new Comparator<QuickTriggerable>() {

		@Override
		public int compare(QuickTriggerable lhs, QuickTriggerable rhs) {
			Triggerable lhst = lhs.t;
			Triggerable rhst = rhs.t;
			int cmp = Triggerable.triggerablesRootOrdering.compare(lhst, rhst);
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
}