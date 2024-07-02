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
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.javarosa.test.BindBuilderXFormsElement.bind;
import static org.javarosa.test.XFormsElement.body;
import static org.javarosa.test.XFormsElement.head;
import static org.javarosa.test.XFormsElement.html;
import static org.javarosa.test.XFormsElement.input;
import static org.javarosa.test.XFormsElement.mainInstance;
import static org.javarosa.test.XFormsElement.model;
import static org.javarosa.test.XFormsElement.repeat;
import static org.javarosa.test.XFormsElement.t;
import static org.javarosa.test.XFormsElement.title;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import org.javarosa.test.Scenario;
import org.javarosa.xform.parse.XFormParser;
import org.junit.Test;

public class GeoAreaTest {
    @Test
    public void area_isComputedForGeoshape() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("geoshape area", html(
            head(
                title("Geoshape area"),
                model(
                    mainInstance(t("data id=\"geoshape-area\"",
                        t("polygon", "38.253094215699576 21.756382658677467 0 0; 38.25021274773806 21.756382658677467 0 0; 38.25007793942195 21.763892843919166 0 0; 38.25290886154963 21.763935759263404 0 0; 38.25146813817506 21.758421137528785 0 0;"),
                        t("area")
                    )),
                    bind("/data/polygon").type("geoshape"),
                    bind("/data/area").type("decimal").calculate("area(/data/polygon)")
                )
            ),
            body(
                input("/data/polygon")
            )
        ));

        // http://www.mapdevelopers.com/area_finder.php?&points=%5B%5B38.253094215699576%2C21.756382658677467%5D%2C%5B38.25021274773806%2C21.756382658677467%5D%2C%5B38.25007793942195%2C21.763892843919166%5D%2C%5B38.25290886154963%2C21.763935759263404%5D%2C%5B38.25146813817506%2C21.758421137528785%5D%5D
        assertThat(Double.parseDouble(scenario.answerOf("/data/area").getDisplayText()),
            closeTo(151452, 0.5));
    }

    @Test
    public void area_isComputedForGeopointNodeset() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("geopoint nodeset area", html(
            head(
                title("Geopoint nodeset area"),
                model(
                    mainInstance(t("data id=\"geopoint-area\"",
                        t("location",
                            t("point", "38.253094215699576 21.756382658677467 0 0")
                        ),
                        t("location",
                            t("point", "38.25021274773806 21.756382658677467 0 0")
                        ),
                        t("location",
                            t("point", "38.25007793942195 21.763892843919166 0 0")
                        ),
                        t("location",
                            t("point", "38.25290886154963 21.763935759263404 0 0")
                        ),
                        t("location",
                            t("point", "38.25146813817506 21.758421137528785 0 0")
                        ),
                        t("area")
                    )),
                    bind("/data/location/point").type("geopoint"),
                    bind("/data/area").type("decimal").calculate("area(/data/location/point)")
                )
            ),
            body(
                repeat("/data/location",
                    input("/data/location/point")
                )
            )
        ));

        // http://www.mapdevelopers.com/area_finder.php?&points=%5B%5B38.253094215699576%2C21.756382658677467%5D%2C%5B38.25021274773806%2C21.756382658677467%5D%2C%5B38.25007793942195%2C21.763892843919166%5D%2C%5B38.25290886154963%2C21.763935759263404%5D%2C%5B38.25146813817506%2C21.758421137528785%5D%5D
        assertThat(Double.parseDouble(scenario.answerOf("/data/area").getDisplayText()),
            closeTo(151452, 0.5));
    }

    @Test
    public void area_isComputedForString() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("string area", html(
            head(
                title("String area"),
                model(
                    mainInstance(t("data id=\"string-area\"",
                        t("point1", "38.253094215699576 21.756382658677467 0 0"),
                        t("point2", "38.25021274773806 21.756382658677467 0 0"),
                        t("point3", "38.25007793942195 21.763892843919166 0 0"),
                        t("point4", "38.25290886154963 21.763935759263404 0 0"),
                        t("point5", "38.25146813817506 21.758421137528785 0 0"),
                        t("concat"),
                        t("area")
                    )),
                    bind("/data/point1").type("geopoint"),
                    bind("/data/point2").type("geopoint"),
                    bind("/data/point3").type("geopoint"),
                    bind("/data/point4").type("geopoint"),
                    bind("/data/point5").type("geopoint"),
                    bind("/data/concat").type("string").calculate("concat(/data/point1, ';', /data/point2, ';', /data/point3, ';', /data/point4, ';', /data/point5)"),
                    bind("/data/area").type("decimal").calculate("area(/data/concat)")
                )
            ),
            body(
                input("/data/point1")
            )
        ));

        // http://www.mapdevelopers.com/area_finder.php?&points=%5B%5B38.253094215699576%2C21.756382658677467%5D%2C%5B38.25021274773806%2C21.756382658677467%5D%2C%5B38.25007793942195%2C21.763892843919166%5D%2C%5B38.25290886154963%2C21.763935759263404%5D%2C%5B38.25146813817506%2C21.758421137528785%5D%5D
        assertThat(Double.parseDouble(scenario.answerOf("/data/area").getDisplayText()),
            closeTo(151452, 0.5));
    }

    @Test
    public void area_whenShapeHasFewerThanThreePoints_isZero() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("geoshape area", html(
            head(
                title("Geoshape area"),
                model(
                    mainInstance(t("data id=\"geoshape-area\"",
                        t("polygon1", "38.253094215699576 21.756382658677467 0 0;"),
                        t("polygon2", "38.253094215699576 21.756382658677467 0 0; 38.25021274773806 21.756382658677467 0 0;"),
                        t("area1"),
                        t("area2")
                    )),
                    bind("/data/polygon1").type("geoshape"),
                    bind("/data/polygon2").type("geoshape"),
                    bind("/data/area1").type("decimal").calculate("area(/data/polygon1)"),
                    bind("/data/area2").type("decimal").calculate("area(/data/polygon2)")
                )
            ),
            body(
                input("/data/polygon1"),
                input("/data/polygon2")
            )
        ));

        assertThat(Double.parseDouble(scenario.answerOf("/data/area1").getDisplayText()), is(0.0));
        assertThat(Double.parseDouble(scenario.answerOf("/data/area2").getDisplayText()), is(0.0));
    }
}
