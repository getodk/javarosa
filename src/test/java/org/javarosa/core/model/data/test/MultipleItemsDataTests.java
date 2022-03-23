/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.core.model.data.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.MultipleItemsData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import org.junit.Before;
import org.junit.Test;

public class MultipleItemsDataTests {
    QuestionDef question;

    Selection one;
    Selection two;
    Selection three;

   List<Selection> firstTwo;
   List<Selection> lastTwo;
   List invalid;


    /* (non-Javadoc)
     * @see j2meunit.framework.TestCase#setUp()
     */
    @Before
    public void setUp() throws Exception {

        question = new QuestionDef();

        for (int i = 0; i < 4; i++) {
            question.addSelectChoice(new SelectChoice("","Selection" + i, "Selection " + i, false, null));
        }

        one = new Selection("Selection 1");
        one.attachChoice(question);
        two = new Selection("Selection 2");
        two.attachChoice(question);
        three = new Selection("Selection 3");
        three.attachChoice(question);

        firstTwo = new ArrayList<Selection>();
        firstTwo.add(one);
        firstTwo.add(two);

        lastTwo = new ArrayList<Selection>();
        lastTwo.add(two);
        lastTwo.add(three);

        invalid = new ArrayList<Object>();
        invalid.add(three);
        invalid.add(new Integer(12));
        invalid.add(one);
    }

    @Test
    public void testGetData() {
        SelectOneData data = new SelectOneData(one);
        assertEquals("SelectOneData's getValue returned an incorrect SelectOne", data.getValue(), one);

    }

    @Test
    public void testSetData() {
        MultipleItemsData data = new MultipleItemsData(firstTwo);
        data.setValue(lastTwo);

        assertTrue("MultipleItemsData did not set value properly. Maintained old value.", !(data.getValue().equals(firstTwo)));
        assertEquals("MultipleItemsData did not properly set value ", data.getValue(), lastTwo);

        data.setValue(firstTwo);
        assertTrue("MultipleItemsData did not set value properly. Maintained old value.", !(data.getValue().equals(lastTwo)));
        assertEquals("MultipleItemsData did not properly reset value ", data.getValue(), firstTwo);

    }

    @Test
    public void testNullData() {
        boolean exceptionThrown = false;
        MultipleItemsData data = new MultipleItemsData();
        data.setValue(firstTwo);
        try {
            data.setValue(null);
        } catch (NullPointerException e) {
            exceptionThrown = true;
        }
        assertTrue("MultipleItemsData failed to throw an exception when setting null data", exceptionThrown);
        assertTrue("MultipleItemsData overwrote existing value on incorrect input", data.getValue().equals(firstTwo));
    }

    @Test
    public void testVectorImmutability() {
        MultipleItemsData data = new MultipleItemsData(firstTwo);
        Selection[] copy = new Selection[firstTwo.size()];
        firstTwo.toArray(copy);
        firstTwo.set(0, two);
        firstTwo.remove(1);

        List<Selection> internal = (List<Selection>)data.getValue();

        assertVectorIdentity("External Reference: ", internal, copy);

        data.setValue(lastTwo);
        List<Selection> start = (List<Selection>)data.getValue();

        Selection[] external = new Selection[start.size()];
        start.toArray(external);

        start.remove(1);
        start.set(0, one);

        assertVectorIdentity("Internal Reference: ", (List<Selection>)data.getValue(), external);
    }

    private void assertVectorIdentity(String messageHeader, List<Selection> v, Selection[] a) {

        assertEquals(messageHeader + "MultipleItemsData's internal representation was violated. Vector size changed.",v.size(),a.length);

        for(int i = 0 ; i < v.size(); ++i) {
            Selection internalValue = v.get(i);
            Selection copyValue = a[i];

            assertEquals(messageHeader + "MultipleItemsData's internal representation was violated. Element " + i + "changed.",internalValue,copyValue);
        }
    }

    @Test
    public void testBadDataTypes() {
        boolean failure = false;
        MultipleItemsData data = new MultipleItemsData(firstTwo);
        try {
            data.setValue(invalid);
            data = new MultipleItemsData(invalid);
        } catch(Exception e) {
            failure = true;
        }
        assertTrue("MultipleItemsData did not throw a proper exception while being set to invalid data.",failure);

        Selection[] values = new Selection[firstTwo.size()];
        firstTwo.toArray(values);
        assertVectorIdentity("Ensure not overwritten: ", (List<Selection>)data.getValue(), values);
    }
}
