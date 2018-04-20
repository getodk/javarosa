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

import static org.javarosa.core.util.GeoUtils.calculateAreaOfGPSPolygonOnEarthInSquareMeters;
import static org.javarosa.core.util.GeoUtils.calculateDistance;
import static org.junit.Assert.assertEquals;

public class GeoUtilsTest {
  private static final Logger logger = LoggerFactory.getLogger(GeoUtilsTest.class);

  /**
   * |\      /-----------------------------------------------
   * | \    /                                               |
   * |  \  /                                                |
   * |   \/                                                 |
   * |                                                      |
   * |                                                      |
   * |                                                      |
   * |-------------------------------------------------------
   *
   *
   * Tests that the enclosed area is the same as in:
   *
   * http://www.mapdevelopers.com/area_finder.php?&points=%5B%5B38.253094215699576%2C21.756382658677467%5D%2C%5B38.25021274773806%2C21.756382658677467%5D%2C%5B38.25007793942195%2C21.763892843919166%5D%2C%5B38.25290886154963%2C21.763935759263404%5D%2C%5B38.25146813817506%2C21.758421137528785%5D%5D
   *
   *
   */
  @Test public void testCalculateAreaOfGPSPolygonOnEarthInSquareMeters_1st_Route() {
    double[][] points = {
            {38.253094215699576,21.756382658677467},
            {38.25021274773806, 21.756382658677467},
            {38.25007793942195, 21.763892843919166},
            {38.25290886154963, 21.763935759263404},
            {38.25146813817506, 21.758421137528785}
    };

    runTestWith(points, 151452);
  }

  /**
   * ------------------------------------------------------
   *  \                                                   |
   *   \                                                  |
   *    \                                                 |
   *    /                                                 |
   *   /                                                  |
   *  /                                                   |
   * ------------------------------------------------------
   *
   *
   * Tests that the enclosed area is the same as in:
   *
   * http://www.mapdevelopers.com/area_finder.php?&points=%5B%5B38.25304740874071%2C21.75644703234866%5D%2C%5B38.25308110946615%2C21.763377860443143%5D%2C%5B38.25078942453431%2C21.763399318115262%5D%2C%5B38.25090738066984%2C21.756640151397733%5D%2C%5B38.25197740258244%2C21.75892539347842%5D%5D
   *
   *
   */
  @Test public void testCalculateAreaOfGPSPolygonOnEarthInSquareMeters_2nd_Route() {
    double[][] points = {
            {38.25304740874071,21.75644703234866},
            {38.25308110946615,21.763377860443143},
            {38.25078942453431,21.763399318115262},
            {38.25090738066984,21.756640151397733},
            {38.25197740258244,21.75892539347842}
    };

    runTestWith(points, 122755);
  }

/**
   * ------------------------------------------------------
   * |                         ........................./
   * |     .................../
   * |    /
   * |    \...................
   * |                        \........................
   * |                                                 \...
   * ------------------------------------------------------
   *
   *
   * Tests that the enclosed area is the same as in:
   *
   * http://www.mapdevelopers.com/area_finder.php?&points=%5B%5B38.252845204059824%2C21.763313487426785%5D%2C%5B38.25303055837213%2C21.755867675201443%5D%2C%5B38.25072202094234%2C21.755803302185086%5D%2C%5B38.25062091543717%2C21.76294870700076%5D%2C%5B38.25183417221606%2C21.75692982997134%5D%5D
   *
   *
 */
  @Test public void testCalculateAreaOfGPSPolygonOnEarthInSquareMeters_3rd_Route() {
    double[][] points = {
            {38.252845204059824,21.763313487426785},
            {38.25303055837213,21.755867675201443},
            {38.25072202094234,21.755803302185086},
            {38.25062091543717,21.76294870700076},
            {38.25183417221606,21.75692982997134}
    };

    runTestWith(points, 93912);
  }

  @Test public void area1() {
    double[][] points = {
            {0, 0},
            {0, -1},
            {-1, -1},
            {-1, 0},
            {0, 0}
    };

    runTestWith(points, 93912);
  }

  @Test public void oneDegreeLatChgAtEquator() {
      assertEquals(110_574, calculateDistance(getLatLongs(new double[][]{
          {0, 0},
          {1, 0},
      })), 1);
  }

  @Test public void oneDegreeLongChgAtEquator() {
      assertEquals(111_320, calculateDistance(getLatLongs(new double[][]{
          {0, 0},
          {0, 1},
      })), 1);
  }

  @Test public void oneDegreeLatChgAt15Lat() {
      assertEquals(110_649, calculateDistance(getLatLongs(new double[][]{
          {14.5, 0},
          {15.5, 0},
      })), 1);
  }

  @Test public void oneDegreeLongChgAt15Lat() {
      assertEquals(107_551, calculateDistance(getLatLongs(new double[][]{
          {15, 0},
          {15, 1},
      })), 1);
  }

  @Test public void oneDegreeLatChgAt75Lat() {
      assertEquals(110_618, calculateDistance(getLatLongs(new double[][]{
          {74.5, 0},
          {75.5, 0},
      })), 1);
  }

    @Test public void oneDegreeLongChgAt90Lat() {
        assertEquals(0, calculateDistance(getLatLongs(new double[][]{
            {90, 0},
            {90, 1},
        })), 0.1);
    }

  private void runTestWith(double[][] points, long expectedArea) {
      List<GeoUtils.LatLong> latLongs = getLatLongs(points);

    double area = calculateAreaOfGPSPolygonOnEarthInSquareMeters(latLongs);

    logger.info("Area in m2: {}", area);

    assertEquals(expectedArea, area, 1);
  }

    private List<GeoUtils.LatLong> getLatLongs(double[][] points) {
        List<GeoUtils.LatLong> latLongs = new ArrayList<>();
        for (double[] point : points) {
          latLongs.add(new GeoUtils.LatLong(point[0], point[1]));
        }
        return latLongs;
    }
}
