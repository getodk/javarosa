/*
 * Copyright (C) 2012-14 Dobility, Inc.
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

package org.javarosa.core.util;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.InstanceInitializationFactory;
import org.javarosa.core.model.instance.TreeElement;
import org.junit.Test;

import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.javarosa.xform.parse.FormParserHelper.parse;
import static org.junit.Assert.assertEquals;

/**
 * Author: Meletis Margaritis
 * Date: 8/4/14
 * Time: 3:40 PM
 */
public class GeoShapeAreaTest {

    @Test public void testGeoShapeSupportForEnclosedArea() throws Exception {
        // Read the form definition
        final FormDef formDef = parse(r("area.xml")).formDef;

        // Trigger all calculations
        formDef.initialize(true, new InstanceInitializationFactory());

        // Check the results. The data and expected results come from GeoUtilsTest.
        TreeElement root = formDef.getMainInstance().getRoot();

        IAnswerData area = root.getChildAt(1).getValue();
        assertEquals(151_452, (Double) area.getValue(), 0.5);

        IAnswerData distance = root.getChildAt(2).getValue();
        assertEquals(1_801, (Double) distance.getValue(), 0.5);
    }

    @Test
    public void testAreaWithLessThanThreePoints() throws Exception {
        // Read the form definition
        final FormDef formDef = parse(r("area_with_less_than_three_points.xml")).formDef;

        // Trigger all calculations
        formDef.initialize(true, new InstanceInitializationFactory());

        // Check the results. The data and expected results come from GeoUtilsTest.
        TreeElement root = formDef.getMainInstance().getRoot();

        IAnswerData area = root.getChildAt(2).getValue();
        assertEquals(0, area.getValue());

        area = root.getChildAt(3).getValue();
        assertEquals(0, area.getValue());
    }
}
