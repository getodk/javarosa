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

import static org.hamcrest.Matchers.closeTo;
import static org.javarosa.core.util.BindBuilderXFormsElement.bind;
import static org.javarosa.core.util.GeoUtils.EARTH_EQUATORIAL_CIRCUMFERENCE_METERS;
import static org.javarosa.core.util.XFormsElement.body;
import static org.javarosa.core.util.XFormsElement.head;
import static org.javarosa.core.util.XFormsElement.html;
import static org.javarosa.core.util.XFormsElement.input;
import static org.javarosa.core.util.XFormsElement.mainInstance;
import static org.javarosa.core.util.XFormsElement.model;
import static org.javarosa.core.util.XFormsElement.repeat;
import static org.javarosa.core.util.XFormsElement.t;
import static org.javarosa.core.util.XFormsElement.title;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import org.javarosa.core.test.Scenario;
import org.junit.Test;

public class GeoDistanceTest {
    private static final double NINETY_DEGREES_ON_EQUATOR_KM = EARTH_EQUATORIAL_CIRCUMFERENCE_METERS / 4;

    @Test
    public void distance_isComputedForGeopointNodeset() throws IOException {
        Scenario scenario = Scenario.init("geopoint nodeset distance", html(
            head(
                title("Geopoint nodeset distance"),
                model(
                    mainInstance(t("data id=\"geopoint-distance\"",
                        t("location",
                            t("point", "0 1 0 0")
                        ),
                        t("location",
                            t("point", "0 91 0 0")
                        ),
                        t("distance")
                    )),
                    bind("/data/location/point").type("geopoint"),
                    bind("/data/distance").type("decimal").calculate("distance(/data/location/point)")
                )
            ),
            body(
                repeat("/data/location",
                    input("/data/location/point")
                )
            )
        ));

        assertThat(Double.parseDouble(scenario.answerOf("/data/distance").getDisplayText()),
            closeTo(NINETY_DEGREES_ON_EQUATOR_KM, 1e-7));
    }

    @Test
    public void distance_isComputedForGeotrace() throws IOException {
        Scenario scenario = Scenario.init("geotrace distance", html(
            head(
                title("Geotrace distance"),
                model(
                    mainInstance(t("data id=\"geotrace-distance\"",
                        t("line", "0 1 0 0; 0 91 0 0;"),
                        t("distance")
                    )),
                    bind("/data/line").type("geotrace"),
                    bind("/data/distance").type("decimal").calculate("distance(/data/line)")
                )
            ),
            body(
                input("/data/line")
            )
        ));

        assertThat(Double.parseDouble(scenario.answerOf("/data/distance").getDisplayText()),
            closeTo(NINETY_DEGREES_ON_EQUATOR_KM, 1e-7));
    }

    @Test
    public void distance_isComputedForGeoshape() throws IOException {
        Scenario scenario = Scenario.init("geoshape distance", html(
            head(
                title("Geoshape distance"),
                model(
                    mainInstance(t("data id=\"geoshape-distance\"",
                        t("polygon", "0 1 0 0; 0 91 0 0; 0 1 0 0;"),
                        t("distance")
                    )),
                    bind("/data/polygon").type("geoshape"),
                    bind("/data/distance").type("decimal").calculate("distance(/data/polygon)")
                )
            ),
            body(
                input("/data/polygon")
            )
        ));

        assertThat(Double.parseDouble(scenario.answerOf("/data/distance").getDisplayText()),
            closeTo(NINETY_DEGREES_ON_EQUATOR_KM * 2, 1e-7));
    }
}
