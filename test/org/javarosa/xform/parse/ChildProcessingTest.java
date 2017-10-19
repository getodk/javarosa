package org.javarosa.xform.parse;

import org.junit.Test;
import org.kxml2.kdom.Element;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.kxml2.kdom.Node.COMMENT;
import static org.kxml2.kdom.Node.ELEMENT;

public class ChildProcessingTest {
    @Test public void worksWithOneChild() {
        Element el = new Element();
        el.addChild(ELEMENT, el.createElement(null, "Child Name"));
        assertTrue(XFormParser.allChildrenAreElementsAndHaveTheSameName(el));
    }

    @Test public void worksWithTwoMatchingChildren() {
        Element el = new Element();
        el.addChild(ELEMENT, el.createElement(null, "Child Name"));
        el.addChild(ELEMENT, el.createElement(null, "Child Name"));
        assertTrue(XFormParser.allChildrenAreElementsAndHaveTheSameName(el));
    }

    @Test public void worksWithTwoNotMatchingChildren() {
        Element el = new Element();
        el.addChild(ELEMENT, el.createElement(null, "Child Name 1"));
        el.addChild(ELEMENT, el.createElement(null, "Child Name 2"));
        assertFalse(XFormParser.allChildrenAreElementsAndHaveTheSameName(el));
    }

    @Test public void WorksWithNoChildren() {
        Element el = new Element();
        assertFalse(XFormParser.allChildrenAreElementsAndHaveTheSameName(el));
    }

    @Test public void discoversNonElementChild() {
        Element el = new Element();
        el.addChild(COMMENT, "A string");
        assertFalse(XFormParser.allChildrenAreElementsAndHaveTheSameName(el));
    }

}
