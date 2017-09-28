package org.javarosa.xform.parse;

import org.javarosa.core.util.CacheTable;
import org.kxml2.kdom.Element;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import static org.kxml2.kdom.Node.ELEMENT;
import static org.kxml2.kdom.Node.TEXT;

class XmlTextConsolidator {

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
            ElementChildDeleter.delete(e, removeIndexes);
        }
    }

    private static boolean nonBlank(String s) {
        return !s.trim().isEmpty();
    }

    private static String maybeInternedString(CacheTable<String> stringCache, String accumulate) {
        return stringCache == null ? accumulate : stringCache.intern(accumulate);
    }

}
