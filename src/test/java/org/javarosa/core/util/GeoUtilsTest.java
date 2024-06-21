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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.javarosa.core.util.GeoUtils.EARTH_EQUATORIAL_CIRCUMFERENCE_METERS;
import static org.junit.Assert.assertEquals;

/**
 * Tests features of GeoUtils. The tests whose names start with areaAndDistanceAreCorrectForPath
 * use data from www.mapdevelopers.com/area_finder.php. They test that the enclosed area,
 * and distance between the points, match the area and perimeter from that web site. A link
 * for each of the data sets is provided with each such test. At the web site, click inside
 * the polygon to show its area and perimeter.
 */
public class GeoUtilsTest {
    private static final Logger logger = LoggerFactory.getLogger(GeoUtilsTest.class);

    /**
     * <a href="http://www.mapdevelopers.com/area_finder.php?&points=%5B%5B38.253094215699576%2C21.756382658677467%5D%2C%5B38.25021274773806%2C21.756382658677467%5D%2C%5B38.25007793942195%2C21.763892843919166%5D%2C%5B38.25290886154963%2C21.763935759263404%5D%2C%5B38.25146813817506%2C21.758421137528785%5D%5D">mapdevelopers.com</a>
     */
    @Test
    public void areaAndDistanceAreCorrectForPath1() {
        checkAreaAndDistance(new double[][]{
            {38.253094215699576, 21.756382658677467},
            {38.25021274773806,  21.756382658677467},
            {38.25007793942195,  21.763892843919166},
            {38.25290886154963,  21.763935759263404},
            {38.25146813817506,  21.758421137528785}
        }, 151_452, 1_801);
    }

    /**
     * <a href="http://www.mapdevelopers.com/area_finder.php?&points=%5B%5B38.25304740874071%2C21.75644703234866%5D%2C%5B38.25308110946615%2C21.763377860443143%5D%2C%5B38.25078942453431%2C21.763399318115262%5D%2C%5B38.25090738066984%2C21.756640151397733%5D%2C%5B38.25197740258244%2C21.75892539347842%5D%5D">mapdevelopers.com</a>
     */
    @Test
    public void areaAndDistanceAreCorrectForPath2() {
        checkAreaAndDistance(new double[][]{
            {38.25304740874071, 21.75644703234866},
            {38.25308110946615, 21.763377860443143},
            {38.25078942453431, 21.763399318115262},
            {38.25090738066984, 21.756640151397733},
            {38.25197740258244, 21.75892539347842}
        }, 122_755, 1_685);
    }

    /**
     * <a href="http://www.mapdevelopers.com/area_finder.php?&points=%5B%5B38.252845204059824%2C21.763313487426785%5D%2C%5B38.25303055837213%2C21.755867675201443%5D%2C%5B38.25072202094234%2C21.755803302185086%5D%2C%5B38.25062091543717%2C21.76294870700076%5D%2C%5B38.25183417221606%2C21.75692982997134%5D%5D">mapdevelopers.com</a>
     */
    @Test
    public void areaAndDistanceAreCorrectForPath3() {
        checkAreaAndDistance(new double[][]{
            {38.252845204059824, 21.763313487426785},
            {38.25303055837213,  21.755867675201443},
            {38.25072202094234,  21.755803302185086},
            {38.25062091543717,  21.76294870700076},
            {38.25183417221606,  21.75692982997134}
        }, 93_912, 2076);
    }

    @Test
    public void oneDegreeAroundAtEquator() {
        assertEquals(EARTH_EQUATORIAL_CIRCUMFERENCE_METERS / 360, distance(new double[][]{
            {0, 0},
            {0, 1},
        }), 1e-6);
    }

    @Test
    public void ninetyDegreesAroundAtEquator() {
        assertEquals(EARTH_EQUATORIAL_CIRCUMFERENCE_METERS / 4, distance(new double[][]{
            {0, 0},
            {0, 90},
        }), 1e-6);
    }

    @Test
    public void oneDegreeLongChgAt90Lat() {
        assertEquals(0, distance(new double[][]{
            {90, 0},
            {90, 1},
        }), 1e-6);
    }

    /*
    https://www.mapdevelopers.com/area_finder.php?polygons=%5B%5B%5B%5B38.253094215699576%2C21.756382658677467%5D%2C%5B38.25021274773806%2C21.756382658677467%5D%2C%5B38.25007793942195%2C21.763892843919166%5D%2C%5B38.25290886154963%2C21.763935759263404%5D%2C%5B38.25146813817506%2C21.758421137528785%5D%2C%5B38.253094215699576%2C21.756382658677467%5D%5D%2C%22%230000FF%22%2C%22%23FF0000%22%2C0.4%5D%5D
     */
    @Test
    public void pointIsInPolygon() {
        checkPointInPolygon(38.25081280703969, 21.760299116099116,
            new double[][]{
                {38.253094215699576, 21.756382658677467},
                {38.25021274773806,  21.756382658677467},
                {38.25007793942195,  21.763892843919166},
                {38.25290886154963,  21.763935759263404},
                {38.25146813817506,  21.758421137528785},
                {38.253094215699576, 21.756382658677467} // last point in geoshape must match first
        }, true);
    }

    @Test
    public void point2IsInPolygon() {
        checkPointInPolygon(38.251790, 21.756845,
            new double[][]{
                {38.253094215699576, 21.756382658677467},
                {38.25021274773806,  21.756382658677467},
                {38.25007793942195,  21.763892843919166},
                {38.25290886154963,  21.763935759263404},
                {38.25146813817506,  21.758421137528785},
                {38.253094215699576, 21.756382658677467} // last point in geoshape must match first
            }, true);
    }
    @Test
    public void pointIsNotInPolygon() {
        checkPointInPolygon(38.252062644683356, 21.758894013612437,
            new double[][]{
                {38.253094215699576, 21.756382658677467},
                {38.25021274773806,  21.756382658677467},
                {38.25007793942195,  21.763892843919166},
                {38.25290886154963,  21.763935759263404},
                {38.25146813817506,  21.758421137528785},
                {38.253094215699576, 21.756382658677467} // last point in geoshape must match first
            }, false);
    }

    private double distance(double[][] points) {
        return GeoUtils.calculateDistance(getLatLongs(points));
    }

    private void checkAreaAndDistance(double[][] points, int expectedArea, int expectedDistance) {
        List<GeoUtils.LatLong> latLongs = new ArrayList<>();
        for (double[] point : points) {
            latLongs.add(new GeoUtils.LatLong(point[0], point[1]));
        }

        double area = GeoUtils.calculateAreaOfGPSPolygonOnEarthInSquareMeters(latLongs);
        double distance = GeoUtils.calculateDistance(latLongs);
        logger.info("Area: {} square meters; perimeter: {} meters", area, distance);
        assertEquals("Area", expectedArea, area, 0.5);
        assertEquals("Distance", expectedDistance, distance, 0.5);
    }

    private List<GeoUtils.LatLong> getLatLongs(double[][] points) {
        List<GeoUtils.LatLong> latLongs = new ArrayList<>();
        for (double[] point : points) {
            latLongs.add(new GeoUtils.LatLong(point[0], point[1]));
        }
        return latLongs;
    }

    private void checkPointInPolygon(double latitude, double longitude, double[][] points, boolean expectedResult) {
        List<GeoUtils.LatLong> latLongs = new ArrayList<>();
        for (double[] point : points) {
            latLongs.add(new GeoUtils.LatLong(point[0], point[1]));
        }

        GeoUtils.LatLong point = new GeoUtils.LatLong(latitude, longitude);
        boolean result = GeoUtils.calculateIsPointInGPSPolygon(point, latLongs);
        assertEquals(expectedResult, result);
    }
}
