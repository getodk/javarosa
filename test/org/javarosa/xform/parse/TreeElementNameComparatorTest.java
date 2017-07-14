package org.javarosa.xform.parse;

import org.javarosa.core.model.instance.TreeElement;
import org.junit.Test;

import java.util.HashMap;

import static org.javarosa.core.model.instance.utils.TreeElementNameComparator.elementMatchesName;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TreeElementNameComparatorTest {

    private static final String NS_ORX = "http://openrosa.org/xforms";
    private static final String PREFIX_ORX = "orx";

    @Test public void wildcardMatches() {
        assertTrue(elementMatchesName(new TreeElement("a"), "*"));
    }

    @Test public void simpleMatches() {
        assertTrue(elementMatchesName(new TreeElement("a"), "a"));
    }

    @Test public void simpleMisMatches() {
        assertFalse(elementMatchesName(new TreeElement("a"), "b"));
    }

    @Test public void explicitMatches() {
        assertTrue(elementMatchesName(createTreeElement("a", NS_ORX, PREFIX_ORX), "orx:a"));
    }

    @Test public void explicitMismatchesName() {
        assertFalse(elementMatchesName(createTreeElement("a", NS_ORX, PREFIX_ORX), "orx:b"));
    }

    @Test public void explicitMismatchesPrefix() {
        assertFalse(elementMatchesName(createTreeElement("a", NS_ORX, PREFIX_ORX), "orx2:b"));
    }

    @Test public void explicitMismatchesNoNsElement() {
        assertFalse(elementMatchesName(new TreeElement("a"), "orx:a"));
    }

    /**
     * Creates a TreeElement for testing.
     * <p>
     * JavaRosa creates TreeElements that don’t contain prefixes. Rather, they have the namespace
     * associated with the prefix.
     *
     * @param name the name of the element, without any prefix
     * @param namespace the namespace, e.g., http://openrosa.org/xforms, or null
     * @param prefix the prefix we are pretending has been associated with namespace, or null
     * @return a TreeElement
     */
    private TreeElement createTreeElement(String name, final String namespace, final String prefix) {
        TreeElement te = new TreeElement(name);
        if (namespace != null && prefix != null) {
            te.setNamespace(namespace);
            te.setNamespacePrefixesByUri(new HashMap<String, String>() {{ put(namespace, prefix); }});
        }
        return te;
    }
}
