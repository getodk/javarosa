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
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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
    private TreeElement c0;
    private TreeElement c1;
    private List<TreeElement> teList;

    @Before public void setUp() {
        NameIndex.logWhenSetInvalid = true;
        cl = new TreeElementChildrenList();
        a0 = new TreeElement("a");
        a1 = new TreeElement("a", 1);
        b0 = new TreeElement("b");
        b1 = new TreeElement("b", 1);
        c0 = new TreeElement("c");
        c1 = new TreeElement("c", 1);
        for (TreeElement nsTe : Arrays.asList(c0, c1)) {
            nsTe.setNamespace("http://openrosa.org/xforms");
            nsTe.setNamespacePrefix("orx");
        }
        a1With0Mult = new TreeElement("a", 0);
        teList = Arrays.asList(a0, a1, b0, b1, c0, c1);
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
        cl.addAll(teList);
        cl.remove("b", 0);
        assertEquals(teList.size() - 1, cl.size());
        assertSame(b1, cl.get("b", 1));
        assertTrue(cl.indexIsValid("a"));
        assertFalse(cl.indexIsValid("b"));
    }

    @Test public void canRemoveAll() {
        cl.addAll(teList);
        assertEquals(3, cl.nameToNameIndex.size());

        cl.removeAll("a");
        assertEquals(teList.size() - 2, cl.size());
        assertEquals(2, cl.nameToNameIndex.size());

        assertTrue(cl.indexIsValid("b"));
        NameIndex ni = cl.nameToNameIndex.get("b");
        assertEquals(0, ni.startingIndex(true));
        assertEquals(2, ni.size(true));
    }

    @Test public void canClear() {
        cl.addAll(teList);
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
        cl.addAll(teList);
        assertTrue(cl.indexIsValid("a"));
        assertTrue(cl.indexIsValid("b"));
    }

    @Test public void canGetWithWildcard() {
        cl.addAll(teList);
        List<TreeElement> elements = cl.get(TreeReference.NAME_WILDCARD);
        assertEquals(teList, elements);
    }

    @Test public void canGetEmptyListWhenNoNameMatch() {
        cl.addAll(teList);
        assertEquals(0, cl.get("not-present-name").size());
    }

    @Test public void canGetAndCountUsingNamespaces() {
        cl.addInOrder(a0);
        cl.addInOrder(c0);
        cl.addInOrder(c1);

        assertEquals(2, cl.get("c").size());
        assertEquals(2, cl.get("orx:c").size());

        assertSame(c0, cl.get("c", 0));
        assertSame(c0, cl.get("orx:c", 0));
        assertSame(c1, cl.get("c", 1));
        assertSame(c1, cl.get("orx:c", 1));

        assertEquals(2, cl.getCount("c"));
        assertEquals(2, cl.getCount("orx:c"));

        assertEquals(0, cl.get("wrong:c").size());
    }

    @Test public void canRemoveNamespacedElementWithoutPrefix() {
        cl.addAll(teList);
        cl.removeAll("c");
        assertEquals(teList.size() - 2, cl.size());
        assertEquals(2, cl.nameToNameIndex.size());
    }

    @Test @Ignore public void canRemoveNamespacedElementWithPrefix() {
        cl.addAll(teList);
        cl.removeAll("orx:c");
        assertEquals(teList.size() - 2, cl.size());
        assertEquals(2, cl.nameToNameIndex.size());
    }
}
