/*
 * Copyright 2019 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.javarosa.core.model;

import java.util.Comparator;

public class QuickTriggerableComparator implements Comparator<QuickTriggerable> {
    static final QuickTriggerableComparator INSTANCE = new QuickTriggerableComparator();

    private QuickTriggerableComparator() {
    }

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
}
