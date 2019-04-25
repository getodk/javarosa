package org.javarosa.xform.parse;

import org.kxml2.kdom.Element;

import java.util.Collection;

class ElementChildDeleter {
    private static final int NUM_DELETIONS_PERFORMANCE_THRESHOLD = 100;
    private static final int NUM_CHILDREN_PERFORMANCE_THRESHOLD = 100;

    /**
     * Deletes the children with the specified indexes using one of two algorithms.
     * @param parent the parent element
     * @param removeIndexes the indexes of the children to be deleted. The Collection implementation
     *                      should have a fast <code>contains</code> method, as does a Set.
     */
    static void delete(Element parent, Collection<Integer> removeIndexes) {
        if (highVolumeApproachNeeded(removeIndexes.size(), parent.getChildCount())) {
            rebuildChildrenAndAttributes(parent, removeIndexes);
        } else {
            removeDeletedChildren(parent, removeIndexes);
        }
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
     * @param parent        The element whose children and attributes are to be rebuilt
     * @param removeIndexes the indexes of the children to be removed
     */
    private static void rebuildChildrenAndAttributes(Element parent, Collection<Integer> removeIndexes) {
        // Save children
        int saveChildCount = 0;
        final Object[] savedChildren   = new Object[parent.getChildCount()];
        final int[]    savedChildTypes = new int[parent.getChildCount()];
        for (int i = 0; i < parent.getChildCount(); ++i) {
            if (!removeIndexes.contains(i)) {
                savedChildren[saveChildCount] = parent.getChild(i);
                savedChildTypes[saveChildCount++] = parent.getType(i);
            }
        }

        // Save attributes
        final int attributeCount = parent.getAttributeCount();
        final String[] attribNameSpaces   = new String[attributeCount];
        final String[] attribNames        = new String[attributeCount];
        final String[] attribValues       = new String[attributeCount];
        for (int i = 0; i < attributeCount; ++i) {
            attribNameSpaces[i] = parent.getAttributeNamespace(i);
            attribNames     [i] = parent.getAttributeName(i);
            attribValues    [i] = parent.getAttributeValue(i);
        }

        parent.clear(); // Removes all children and attributes

        // Restore children
        for (int i = 0; i < saveChildCount; ++i) {
            parent.addChild(savedChildTypes[i], savedChildren[i]);
        }

        // Restore attributes
        for (int i = 0; i < attributeCount; ++i) {
            parent.setAttribute(attribNameSpaces[i], attribNames[i], attribValues[i]);
        }
    }

    /**
     * Removes the unwanted children using a simple approach that performs adequately when the number
     * of children, or number of deletions, are small.
     *
     * @param parent        the element from which to delete children
     * @param removeIndexes the indexes of the children to delete
     */
    private static void removeDeletedChildren(Element parent, Collection<Integer> removeIndexes) {
        for (int i = parent.getChildCount() - 1; i >= 0; i--) {
            if (removeIndexes.contains(i)) {
                parent.removeChild(i);  // This is very expensive
            }
        }
    }
}
