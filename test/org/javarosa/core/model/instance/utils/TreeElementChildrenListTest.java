/*
 * Copyright 2018 Nafundi
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

package org.javarosa.core.model.instance.utils;

import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TreeElementChildrenListTest {
    private TreeElementChildrenList cl;
    private TreeElement a0;
    private TreeElement a1;
    private TreeElement a1With0Mult;
    private TreeElement b0;
    private TreeElement b1;
    private List<TreeElement> abList;

    @Before public void setUp() {
        NameIndex.logWhenSetInvalid = true;
        cl = new TreeElementChildrenList();
        a0 = new TreeElement("a");
        a1 = new TreeElement("a", 1);
        b0 = new TreeElement("b");
        b1 = new TreeElement("b", 1);
        a1With0Mult = new TreeElement("a", 0);
        abList = Arrays.asList(a0, a1, b0, b1);
    }

    @Test public void canAdd() {
        cl.add(0, a0);
        cl.add(1, a1);
        assertEquals(2, cl.size());
        assertTrue(cl.indexIsValid("a"));
        assertSame(a0, cl.get(0));
        assertSame(a1, cl.get(1));
        Iterator<TreeElement> i = cl.iterator();
        assertSame(a0, i.next());
    }

    @Test public void canAddInOrder() {
        cl.addInOrder(a0);
        cl.addInOrder(a1);
        assertEquals(2, cl.size());
        assertTrue(cl.indexIsValid("a"));
    }

    @Test public void canAddInOrderWithATemplate() {
        a0.setMult(TreeReference.INDEX_TEMPLATE);
        cl.addInOrder(a0);
        cl.addInOrder(a1With0Mult);
        assertEquals(2, cl.size());
        assertTrue(cl.indexIsValid("a"));
        assertEquals(1, cl.getCount("a"));
        List<TreeElement> allA = cl.get("a");
        assertEquals(1, allA.size());
        assertSame(a1With0Mult, allA.get(0));
        TreeElement one = cl.get("a", TreeReference.INDEX_TEMPLATE);
        assertSame(a0, one);
        TreeElement two = cl.get("a", 0);
        assertSame(a1With0Mult, two);
        TreeElement none = cl.get("a", 1);
        assertNull(none);
    }

    @Test public void canRemoveByIndex() {
        cl.addInOrder(a0);
        cl.addInOrder(a1);
        cl.remove(0);
        assertEquals(1, cl.size());
        assertSame(a1, cl.get("a", 1));
        assertFalse(cl.indexIsValid("a"));
    }

    @Test public void canRemoveByElement() {
        cl.addInOrder(a0);
        cl.addInOrder(a1);
        cl.remove(a0);
        assertEquals(1, cl.size());
        assertSame(a1, cl.get("a", 1));
        assertFalse(cl.indexIsValid("a"));
    }

    @Test public void canRemoveChildAndAdjustSiblingMults() {
        cl.addInOrder(a0);
        cl.addInOrder(a1);
        cl.removeChildAndAdjustSiblingMults(a0);
        assertEquals(1, cl.size());
        assertSame(a1, cl.get("a", 0));
        assertTrue(cl.indexIsValid("a"));
    }

    @Test public void canRemoveByNameAndMult() {
        cl.addAll(abList);
        cl.remove("b", 0);
        assertEquals(3, cl.size());
        assertSame(b1, cl.get("b", 1));
        assertTrue(cl.indexIsValid("a"));
        assertFalse(cl.indexIsValid("b"));
    }

    @Test public void canRemoveAll() {
        cl.addAll(abList);
        assertEquals(4, cl.size());
        assertEquals(2, cl.nameToNameIndex.size());

        cl.removeAll("a");
        assertEquals(2, cl.size());
        assertEquals(1, cl.nameToNameIndex.size());

        assertTrue(cl.indexIsValid("b"));
        assertEquals(1, cl.nameToNameIndex.size());
        NameIndex ni = cl.nameToNameIndex.get("b");
        assertEquals(0, ni.startingIndex(true));
        assertEquals(2, ni.size(true));
    }

    @Test public void canClear() {
        cl.addAll(abList);
        assertEquals(4, cl.size());
        cl.clear();
        assertEquals(0, cl.size());
        assertTrue(cl.isEmpty());
        assertFalse(cl.indexIsValid("a"));
        assertFalse(cl.indexIsValid("b"));
    }

    @Test public void getCountWorksWithInvalidIndex() {
        cl.addInOrder(a0);
        cl.addInOrder(a1With0Mult);
        assertEquals(2, cl.getCount("a"));
    }

    @Test public void canAddAll() {
        cl.addAll(abList);
        assertEquals(4, cl.size());
        assertTrue(cl.indexIsValid("a"));
        assertTrue(cl.indexIsValid("b"));
    }

    @Test public void canGetWithWildcard() {
        cl.addAll(abList);
        List<TreeElement> elements = cl.get(TreeReference.NAME_WILDCARD);
        assertEquals(abList, elements);
    }

}
