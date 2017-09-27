package org.javarosa.xform.parse;

import org.javarosa.core.util.CacheTable;
import org.kxml2.kdom.Element;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import static org.kxml2.kdom.Node.ELEMENT;
import static org.kxml2.kdom.Node.TEXT;

class XmlTextConsolidator {

    private static final int NUM_DELETIONS_PERFORMANCE_THRESHOLD = 100;
    private static final int NUM_CHILDREN_PERFORMANCE_THRESHOLD = 100;

    /**
     * According to Clayton Sims, in Feb 2012:
     * For escaped unicode strings we end up with a lot of cruft, so we really
     * want to go through and convert the kxml parsed text (which have lots of
     * characters each as their own string) into one single string
     */
    static void consolidateText(CacheTable<String> stringCache, Element rootElement) {
        Stack<Element> q = new Stack<>();
        q.push(rootElement);
        while (!q.isEmpty()) {
            final Element e = q.pop();
            final Set<Integer> removeIndexes = new HashSet<>();
            String accumulator = "";
            for (int i = 0; i < e.getChildCount(); ++i) {
                final int type = e.getType(i);
                if (type == TEXT) {
                    accumulator += e.getText(i);
                    removeIndexes.add(i);
                } else {
                    if (type == ELEMENT) {
                        q.add(e.getElement(i));
                    }
                    if (nonBlank(accumulator)) {
                        e.addChild(i++, TEXT, maybeInternedString(stringCache, accumulator));
                    }
                    accumulator = "";
                }
            }
            if (nonBlank(accumulator)) {
                e.addChild(TEXT, maybeInternedString(stringCache, accumulator));
            }
            if (highVolumeApproachNeeded(removeIndexes.size(), e.getChildCount())) {
                rebuildChildrenAndAttributes(e, removeIndexes);
            } else {
                removeDeletedChildren(e, removeIndexes);
            }
        }
    }

    private static boolean nonBlank(String s) {
        return !s.trim().isEmpty();
    }

    private static String maybeInternedString(CacheTable<String> stringCache, String accumulate) {
        return stringCache == null ? accumulate : stringCache.intern(accumulate);
    }

    /**
     * Determines whether to use the simpler, much slower, child deletion method, or the more complicated
     * one, which performs well even with tens of thousands of deletions or children.
     *
     * @param numDeletions the number of deletions to be done
     * @param numChildren  the number of children
     * @return whether to use the high volume approach
     */
    private static boolean highVolumeApproachNeeded(int numDeletions, int numChildren) {
        return numDeletions > NUM_DELETIONS_PERFORMANCE_THRESHOLD ||
                numChildren > NUM_CHILDREN_PERFORMANCE_THRESHOLD;
    }

    /**
     * Avoids expensive Element.removeChild calls by removing all children and re-adding
     * the ones to be kept. Uses Element.clear to remove the children. Since clear
     * also removes the element’s attributes, it’s necessary to save and restore them as well.
     *
     * @param e             The element whose children and attributes are to be rebuilt
     * @param removeIndexes the indexes of the children to be removed
     */
    private static void rebuildChildrenAndAttributes(Element e, Collection<Integer> removeIndexes) {
        // Save children
        int saveChildCount = 0;
        final Object[] savedChildren   = new Object[e.getChildCount()];
        final int[]    savedChildTypes = new int[e.getChildCount()];
        for (int i = 0; i < e.getChildCount(); ++i) {
            if (!removeIndexes.contains(i)) {
                savedChildren[saveChildCount] = e.getChild(i);
                savedChildTypes[saveChildCount++] = e.getType(i);
            }
        }

        // Save attributes
        final int attributeCount = e.getAttributeCount();
        final String[] attribNameSpaces   = new String[attributeCount];
        final String[] attribNames        = new String[attributeCount];
        final String[] attribValues       = new String[attributeCount];
        for (int i = 0; i < attributeCount; ++i) {
            attribNameSpaces[i] = e.getAttributeNamespace(i);
            attribNames     [i] = e.getAttributeName(i);
            attribValues    [i] = e.getAttributeValue(i);
        }

        e.clear(); // Removes all children and attributes

        // Restore children
        for (int i = 0; i < saveChildCount; ++i) {
            e.addChild(savedChildTypes[i], savedChildren[i]);
        }

        // Restore attributes
        for (int i = 0; i < attributeCount; ++i) {
            e.setAttribute(attribNameSpaces[i], attribNames[i], attribValues[i]);
        }
    }

    /**
     * Removes the unwanted children using a simple approach that performs adequately when the number
     * of children, or number of deletions, are small.
     *
     * @param e             the element from which to delete children
     * @param removeIndexes the indexes of the children to delete
     */
    private static void removeDeletedChildren(Element e, Collection<Integer> removeIndexes) {
        for (int i = e.getChildCount() - 1; i >= 0; i--) {
            if (removeIndexes.contains(i)) {
                e.removeChild(i);  // This is very expensive
            }
        }
    }
}
