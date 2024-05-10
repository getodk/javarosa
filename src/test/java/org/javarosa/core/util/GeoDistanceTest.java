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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
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

import java.io.IOException;
import org.hamcrest.number.IsCloseTo;
import org.javarosa.core.test.Scenario;
import org.javarosa.xform.parse.XFormParser;
import org.junit.Test;

public class GeoDistanceTest {
    private static final double NINETY_DEGREES_ON_EQUATOR_KM = EARTH_EQUATORIAL_CIRCUMFERENCE_METERS / 4;

    @Test
    public void distance_isComputedForGeopointNodeset() throws IOException, XFormParser.ParseException {
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
    public void distance_isComputedForGeotrace() throws IOException, XFormParser.ParseException {
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
    public void distance_isComputedForGeoshape() throws IOException, XFormParser.ParseException {
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

    @Test
    public void distance_isComputedForString() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("string distance", html(
            head(
                title("String distance"),
                model(
                    mainInstance(t("data id=\"string-distance\"",
                        t("point1", "38.253094215699576 21.756382658677467 0 0"),
                        t("point2", "38.25021274773806 21.756382658677467 0 0"),
                        t("point3", "38.25007793942195 21.763892843919166 0 0"),
                        t("point4", "38.25290886154963 21.763935759263404 0 0"),
                        t("point5", "38.25146813817506 21.758421137528785 0 0"),
                        t("concat"),
                        t("distance")
                    )),
                    bind("/data/point1").type("geopoint"),
                    bind("/data/point2").type("geopoint"),
                    bind("/data/point3").type("geopoint"),
                    bind("/data/point4").type("geopoint"),
                    bind("/data/point5").type("geopoint"),
                    bind("/data/concat").type("string").calculate("concat(/data/point1, ';', /data/point2, ';', /data/point3, ';', /data/point4, ';', /data/point5)"),
                    bind("/data/distance").type("decimal").calculate("distance(/data/concat)")
                )),
            body(
                input("/data/point1")
            )
        ));

        // http://www.mapdevelopers.com/area_finder.php?&points=%5B%5B38.253094215699576%2C21.756382658677467%5D%2C%5B38.25021274773806%2C21.756382658677467%5D%2C%5B38.25007793942195%2C21.763892843919166%5D%2C%5B38.25290886154963%2C21.763935759263404%5D%2C%5B38.25146813817506%2C21.758421137528785%5D%5D
        assertThat(Double.parseDouble(scenario.answerOf("/data/distance").getDisplayText()),
            IsCloseTo.closeTo(1801, 0.5));
    }

    @Test
    public void distance_isComputedForMultipleArguments() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("string distance", html(
            head(
                title("Multi parameter distance"),
                model(
                    mainInstance(t("data id=\"string-distance\"",
                        t("point1", "38.253094215699576 21.756382658677467 0 0"),
                        t("point3", "38.25007793942195 21.763892843919166 0 0"),
                        t("point4", "38.25290886154963 21.763935759263404 0 0"),
                        t("point5", "38.25146813817506 21.758421137528785 0 0"),
                        t("distance")
                    )),
                    bind("/data/point1").type("geopoint"),
                    bind("/data/point3").type("geopoint"),
                    bind("/data/point4").type("geopoint"),
                    bind("/data/point5").type("geopoint"),
                    bind("/data/distance").type("decimal").calculate("distance(/data/point1, '38.25021274773806 21.756382658677467 0 0', /data/point3, /data/point4, /data/point5)")
                )),
            body(
                input("/data/point1")
            )
        ));

        // http://www.mapdevelopers.com/area_finder.php?&points=%5B%5B38.253094215699576%2C21.756382658677467%5D%2C%5B38.25021274773806%2C21.756382658677467%5D%2C%5B38.25007793942195%2C21.763892843919166%5D%2C%5B38.25290886154963%2C21.763935759263404%5D%2C%5B38.25146813817506%2C21.758421137528785%5D%5D
        assertThat(Double.parseDouble(scenario.answerOf("/data/distance").getDisplayText()),
            IsCloseTo.closeTo(1801, 0.5));
    }

    @Test
    public void distance_whenTraceHasFewerThanTwoPoints_isZero() throws Exception {
        Scenario scenario = Scenario.init("geotrace distance", html(
            head(
                title("Geotrace distance"),
                model(
                    mainInstance(t("data id=\"geotrace-distance\"",
                        t("line", "0 1 0 0;"),
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

        assertThat(Double.parseDouble(scenario.answerOf("/data/distance").getDisplayText()), is(0.0));
    }
}
