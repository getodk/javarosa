package org.javarosa.core.model.instance.utils;

import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;


/** Supports locating {@link TreeElement}s by name. */
public class TreeElementNameComparator {

    /**
     * Determines whether treeElement matches name. If name is a wildcard, it does. If the element’s
     * name matches name (using equals), it does. If neither of those cases are true, a namespace
     * prefix for treeElement, if one can be located, is prepended to treeElement’s name, and that is
     * compared to name with equals.
     *
     * @param treeElement the TreeElement under examination
     * @param name        the name to be compared with treeElement’s name
     * @return whether treeElement matches name
     */
    public static boolean elementMatchesName(TreeElement treeElement, String name) {
        final String namespacePrefix = treeElement.getNamespacePrefix();

        if (name.equals(TreeReference.NAME_WILDCARD)) {
            return true;
        }

        final String elementName = treeElement.getName();

        return elementName.equals(name)
                || (namespacePrefix != null
                && (namespacePrefix + ":" + elementName).equals(name));
    }

}
