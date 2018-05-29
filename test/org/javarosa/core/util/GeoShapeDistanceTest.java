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

package org.javarosa.core.util;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.InstanceInitializationFactory;
import org.javarosa.core.model.instance.TreeElement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.javarosa.core.model.instance.TreeReference.DEFAULT_MULTIPLICITY;
import static org.javarosa.core.util.GeoUtils.EARTH_EQUATORIAL_CIRCUMFERENCE_METERS;
import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.javarosa.xform.parse.FormParserHelper.parse;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class GeoShapeDistanceTest {
    @Parameterized.Parameter(value = 0)
    public String geoType;

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {"shape"},
            {"trace"},
            {"point"},
        });
    }

    /**
     * Shows that the distance function can be used with inputs
     * <a href="https://opendatakit.github.io/xforms-spec/#fn:distance">as specified</a>.
     */
    @Test
    public void parsingAndDistanceIsCorrectForThreeGeoTypes() throws Exception {
        FormDef formDef = parse(r("distance.xml")).formDef;
        formDef.initialize(true, new InstanceInitializationFactory());
        TreeElement root = formDef.getMainInstance().getRoot();
        IAnswerData distance = root.getChild(geoType + "-result", DEFAULT_MULTIPLICITY).getValue();
        assertEquals(EARTH_EQUATORIAL_CIRCUMFERENCE_METERS / 360 + EARTH_EQUATORIAL_CIRCUMFERENCE_METERS / 4,
            (Double) distance.getValue(), 1e-7);
    }
}
